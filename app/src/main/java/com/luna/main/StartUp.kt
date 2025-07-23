package com.luna.main

import android.Manifest
import android.app.Application
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

import com.luna.data.Song
import com.luna.utils.FileOfTheseus
import com.luna.data.MainDatabase

import android.os.Environment
import android.view.ContextThemeWrapper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.Stack

class StartUp: Application() {

//    lateinit var database: MainDatabase
    override fun onCreate() {
        super.onCreate()

        if(hasStoragePermission(this)) {
            CoroutineScope(Dispatchers.IO).launch {
//                dbReset(this@StartUp)
                getAllAudioFiles(this@StartUp)

            }
        }

    }

    fun hasStoragePermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    //TODO: Investigate crashes/incomplete database curation
    suspend fun dbReset (context: Context) = withContext(Dispatchers.IO) {
       val database = MainDatabase(context)
        val writeDB = database.writeMode()

        database.regenerate(writeDB)
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun mediaScan(context: Context, rootPath: String) = withContext(Dispatchers.IO) {
        val root = File(rootPath)
        val audioExtensions = setOf("mp3", "m4a", "aac", "wav", "flac", "ogg")

        fun isAudio(file: File): Boolean {
            return audioExtensions.contains(file.extension.lowercase())
        }

        // Step 1: Load all indexed audio file paths from MediaStore into a HashSet
        val knownPaths = mutableSetOf<String>()
        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Audio.Media.DATA),
            null,
            null,
            null
        )?.use { cursor ->
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            while (cursor.moveToNext()) {
                knownPaths.add(cursor.getString(dataColumn))
            }
        }

        // Step 2: Traverse file tree and collect unindexed audio files
        val stack = ArrayDeque<File>()
        stack.add(root)
        val filesToScan = mutableListOf<File>()

        while (stack.isNotEmpty()) {
            val current = stack.removeLast()

            if (current.isDirectory) {
                current.listFiles()?.reversed()?.forEach { stack.add(it) }
            } else if (isAudio(current)) {
                if (!knownPaths.contains(current.absolutePath)) {
                    filesToScan.add(current)
                }
            }
        }

        // Step 3: Batch size and concurrency limits
        val batchSize = 100
        val concurrency = 4
        val dispatcher = Dispatchers.IO.limitedParallelism(concurrency)

        // Step 4: Scan files in batches concurrently
        coroutineScope {
            val jobs = filesToScan.chunked(batchSize).map { chunk ->
                async(dispatcher) {
                    MediaScannerConnection.scanFile(
                        context,
                        chunk.map { it.absolutePath }.toTypedArray(),
                        null
                    ) { path, uri ->
                        Log.d("MediaScan", "Scanned: $path -> $uri")
                    }
                }
            }
            jobs.awaitAll()
        }
    }


    suspend fun getAllAudioFiles(context: Context) = withContext(Dispatchers.IO) {

        val database = MainDatabase(context)


//        dbReset(context)
        


        val volumes = MediaStore.getExternalVolumeNames(context).toTypedArray()

        val scanJobs = volumes.map { storage ->
            async {
                val path = if (storage == "external_primary") {
                    "/storage/emulated/0"
                } else {
                    "/storage/$storage"
                }
                mediaScan(context, path)  // your suspend mediaScan function
            }
        }

        scanJobs.awaitAll()


//        Log.d("Database", "${db}")

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ALBUM_ARTIST,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.IS_DOWNLOAD,
            MediaStore.Audio.Media.DATA
        )

        val selection = (
                "${MediaStore.Audio.Media.IS_RINGTONE} = 0"
                        + " AND ${MediaStore.Audio.Media.IS_NOTIFICATION} = 0"
                        + " AND ${MediaStore.Audio.Media.IS_ALARM} = 0"
                        + " AND ${MediaStore.Audio.Media.IS_MUSIC} != 0"
                //+ " AND ${MediaStore.Audio.Media.TITLE} LIKE 'T%'"
                )

        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"


        val cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            sortOrder
        )


        // TODO: Investigate MediaStore.Donwloads causing crashes
//        val cursor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            MergeCursor(
//                arrayOf(
//                    context.contentResolver.query(
//                        MediaStore.Downloads.EXTERNAL_CONTENT_URI,
//                        projection,
//                        selection,
//                        null,
//                        sortOrder
//                        //"${MediaStore.Audio.Media.DISPLAY_NAME} ASC"
//                        //"${MediaStore.Audio.Media.MIME_TYPE} = 'audio/mpeg'"
//                    ),
//                    context.contentResolver.query(
//                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
//                        projection,
//                        selection,
//                        null,
//                        sortOrder
//                        //"${MediaStore.Audio.Media.DISPLAY_NAME} DESC"
//                        //"${MediaStore.Audio.Media.MIME_TYPE} = 'audio/mpeg'"
//                    )
//                )
//            )
//        } else {
//            context.contentResolver.query(
//                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
//                projection,
//                null,
//                null,
//                sortOrder
//                //"${MediaStore.Audio.Media.MIME_TYPE} = 'audio/mpeg'"
//            )
//        }

        val actions = mutableListOf<DBActions>()

        cursor?.use {
            val readOnlyDB = database.readOnlyMode()

            val idColumn = it.getColumnIndex(MediaStore.Audio.Media._ID)
            val nameColumn = it.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
            val titleColumn = it.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val artistColumn = it.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val artistIdColumn = it.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID)
            val albumColumn = it.getColumnIndex(MediaStore.Audio.Media.ALBUM)
            val albumIdColumn = it.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
            val albumartistColumn = it.getColumnIndex(MediaStore.Audio.Media.ALBUM_ARTIST)
            val trackColumn = it.getColumnIndex(MediaStore.Audio.Media.TRACK)
            val mimeColumn = it.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE)
            val isDownloadColumn = it.getColumnIndex(MediaStore.Audio.Media.IS_DOWNLOAD)
            val dataColumn = it.getColumnIndex(MediaStore.Audio.Media.DATA)

                while (it.moveToNext()) {
                    val id = it.getLong(idColumn)
                    val name = it.getString(nameColumn)
                    val title = it.getString(titleColumn)
                    val artist = it.getString(artistColumn) ?: "Unknown"
                    val artistId = it.getLong(artistIdColumn)
                    val album = it.getString(albumColumn)
                    val albumId = it.getLong(albumIdColumn)
                    val albumartist = it.getString(albumartistColumn) ?: "Unknown"
                    val track = it.getLong(trackColumn)
                    val mime = it.getString(mimeColumn)
                    val isDownload = it.getLong(isDownloadColumn)
                    val data = it.getString(dataColumn)

                    val uri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    val hash = FileOfTheseus.calculateFileHash(data)

                    val song = Song(
                        hash, id, name, title,
                        artist, artistId, album, albumId, albumartist,
                        track, mime, isDownload, data, uri
                    )

                    Log.d(
                        "Start Up", song.toString()
                    )

                    Log.d("Start Up", "Checking DB")


                    if (!database.isBlacklisted(readOnlyDB, song)) {
                        Log.d("Start Up", "Running DB in write mode")
                        if (database.ifExist(readOnlyDB, "SongList", song)) {
                            Log.d("Start Up", "Running 'File of Theseus'")
                            val entry = database.checkForUpdate(readOnlyDB, song)!!
                            if (song != entry) {
                                Log.d("Start Up", "Attempting to update entry $hash")
                                actions.add(DBActions.Update(song))
                            } else {
                                Log.d("Start Up", "Skipping entry $hash")
                            }
                        } else {
                            Log.d("Start Up", "Adding entry $hash to DB")
                            actions.add(DBActions.Add(song))
                        }
                    }

                }

            readOnlyDB.close()
        }

        val writeToDB = database.writeMode()

        val batchSize = 500
        val concurrency = 4

        val dispatcher = Dispatchers.IO.limitedParallelism(concurrency)

        coroutineScope {
            actions.chunked(batchSize).map { chunk ->
                async (dispatcher) {
                    writeToDB.beginTransaction()
                    try {
                        chunk.forEach { action ->
                            when (action) {
                                is DBActions.Update -> database.updateEntry(writeToDB, "SongList", action.song)
                                is DBActions.Add -> database.appendToTable(writeToDB, "SongList", action.song)
                            }
                        }
                        writeToDB.setTransactionSuccessful()
                    } finally {
                        writeToDB.endTransaction()
                    }
                }
            }.awaitAll()
        }

//        writeToDB.beginTransaction()
//
//        try {
//            actions.forEach { action ->
//                when (action) {
//                    is DBActions.Update -> database.updateEntry(writeToDB, "SongList", action.song)
//                    is DBActions.Add -> database.appendToTable(writeToDB, "SongList", action.song)
//                }
//            }
//
//            writeToDB.setTransactionSuccessful()
//        } catch (e: Exception) {
//            Log.e("Transaction", "Error during DB transaction", e)
//        } finally {
//            writeToDB.endTransaction()
//            writeToDB.close()
//        }

        return@withContext
    }

    sealed class DBActions {
        data class Update(val song: Song): DBActions()
        data class Add(val song: Song): DBActions()
    }
}