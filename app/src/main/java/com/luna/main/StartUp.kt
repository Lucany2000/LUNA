package com.luna.main

import android.app.Application
import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import com.luna.data.Song
import com.luna.utils.FileOfTheseus
import com.luna.data.MainDatabase

class StartUp: Application() {

    lateinit var audioFiles: List<Song>
//    lateinit var db: MainDatabase
    override fun onCreate() {
        super.onCreate()

        CoroutineScope(Dispatchers.IO).launch {
            audioFiles = getAllAudioFiles(this@StartUp)

        }
    }

    private fun getAllAudioFiles(context: Context): List<Song> {

        val audio = mutableListOf<Song>()

//        db = MainDatabase(this)
//        val readOnlyDB = db.readOnlyMode()
//
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

        cursor?.use {
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


                /*
                if (!db.isBlacklisted(readOnlyDB, song)){
                    val writeToDB = db.writeMode()
                    if (db.ifExist(readOnlyDB, "SongList", song)) {
                        val entry = db.checkForUpdate(readOnlyDB, song)
                        if (song != entry) {
                            db.updateEntry(writeToDB, "SongList", song)
                        }
                    } else {
                        db.appendToTable(writeToDB,"SongList", song)
                    }
                }
                 */

                audio.add(song)

                Log.d("Song", "${
                    Song(
                    hash, id, name, title,
                    artist, artistId, album, albumId, albumartist,
                    track, mime, isDownload, data, uri
                )
                }")

            }
        }
        return audio
    }



}