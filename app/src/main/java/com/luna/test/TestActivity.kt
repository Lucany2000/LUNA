package com.luna.test

import android.content.ContentUris
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.luna.data.Song
import com.luna.main.R
import com.luna.utils.FileOfTheseus

class TestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.playground)
        main()
    }

    private fun main() {
//        generateCharLine()
        getAllAudioFiles(this)

    }

    fun generateCharLine() {
        val charLine = findViewById<LinearLayout>(R.id.charLine)

        val alphabet = ('A'..'Z').toMutableList()

        alphabet.addAll('1'..'9')

        alphabet.map {
            val letter = TextView(this@TestActivity)
            letter.text = it.toString()
            letter.height = 100
            letter.width = 100

            letter.gravity = Gravity.CENTER
            letter.textSize = 16f


            val currentColor = ContextCompat.getColor(this, R.color.light_gray)

//            val colorPressed = Color.BLUE

            letter.background = ColorDrawable(currentColor)

            charLine.addView(letter)
        }
    }

    private fun getAllAudioFiles(context: Context): List<Song> {
        //
        val audio = mutableListOf<Song>()

//        val db = MainDatabase(this)
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

                val hash = FileOfTheseus.calculateFileHash("/storage/emulated/0/Music/ｈｅｙ　ｙａ (synthwave80s remix).mp3")


                audio.add(
                    Song(
                    hash, id, name, title,
                    artist, artistId, album, albumId, albumartist,
                    track, mime, isDownload, data, uri
                )
                )

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