package com.luna.main

import android.widget.PopupWindow
import android.Manifest
import android.content.ContentUris
import android.net.Uri
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ScrollView
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.provider.Settings
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import org.bson.Document


import com.luna.data.Song
import com.luna.global.SongOrder
import com.luna.utils.BackEnd
import com.luna.utils.UI
import com.luna.global.MusicPlayer
import com.luna.data.BackUpDB
import com.luna.utils.QueryTable

class MainActivity : AppCompatActivity() {

    private val REQUEST_PERMISSION_CODE = 0
    private var dismissPopupWindow: PopupWindow? = null
    private val letterToFirstWordMap = mutableMapOf<String, LinearLayout>()
    private val queryTable: QueryTable = QueryTable(this)
    private lateinit var generatedSongOrder: List<Song>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_AUDIO
            ) != PackageManager.PERMISSION_GRANTED ||

            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted, request it
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_MEDIA_AUDIO),
                        REQUEST_PERMISSION_CODE
                    )
            } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        REQUEST_PERMISSION_CODE
                    )
            }
        } else {
            main()

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_PERMISSION_CODE) {
            // Check if permissions are granted
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                main()
            } else {
               errorMsg("No Audio Found", this)
            }
        }
    }

    private fun main() {

        val rootLayout = findViewById<LinearLayout>(R.id.rootLayout)

        val audioFiles = getAllAudioFiles(this).distinctBy { listOf(it.getTitle(),it.getArtist(), it.getAlbum()) }

//        val sortedAudioFiles = BackEnd.sort(grabFromData())

//        Log.d("Database", "${grabFromData()}")

        val sortedAudioFiles = BackEnd.sort(audioFiles)

        val charLine = findViewById<LinearLayout>(R.id.charLine)

        val uniqueChars = BackEnd.createKnownAlphabet(sortedAudioFiles)

        val scrollView = findViewById<ScrollView>(R.id.scrollView)

        for (char in uniqueChars) {
            val button = TextView(this)
            button.text = char.toString()
            button.width = 100
            button.height = 100

            button.gravity = Gravity.CENTER
            button.textSize = 16f


            val currentColor = ContextCompat.getColor(this, R.color.light_gray)

            val colorPressed = Color.BLUE

            button.background = ColorDrawable(currentColor)


            button.setOnTouchListener { view, motionEvent ->
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {

                        showBubbleText(view, button.text)
                        button.background = ColorDrawable(colorPressed)

                        scrollToWordStartingWith(button.text.toString(), scrollView)

                        true // Consume the touch event
                    }
                    MotionEvent.ACTION_UP -> {
                        dismissPopupWindow?.dismiss()

                        button.background = ColorDrawable(currentColor)

                        true // Consume the touch event
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        dismissPopupWindow?.dismiss()

                        button.background = ColorDrawable(currentColor)

                        true // Consume the touch event
                    }
                    else -> false
                }
            }

            // Add the button to the LinearLayout
            charLine.addView(button)
        }

        generatedSongOrder = sortedAudioFiles

        //{(0, title). (1, id)}
        val songButtons = sortedAudioFiles.map { song ->
            val button = createSongButton(song)
            val separator = UI.createSeparator(this)

            rootLayout.addView(button)
            rootLayout.addView(separator)

            Pair(button, separator)
        }
//
        songButtons.forEach { (button, separator) ->
            val textview = button.getChildAt(0) as TextView
            val text = textview.text.toString()
            val firstChar = BackEnd.removePrefix(text).firstOrNull()?.uppercase()

            if (firstChar != null && !letterToFirstWordMap.containsKey(firstChar)) {
                letterToFirstWordMap[firstChar] = button
            }
        }

//        for ((key, linearLayout) in letterToFirstWordMap) {
//            val log = linearLayout.getChildAt(0) as TextView
//            Log.d("Song", "${log.text}")
//        }

//        for (song in sortedAudioFiles) {
//            val button = createSongButton(song)
//            rootLayout.addView(button)
//
//            val separator = UI.createSeparator(this)
//            rootLayout.addView(separator)
//        }

    }

    private fun scrollToWordStartingWith(letter: String, scrollView: ScrollView) {
        val textView: LinearLayout? = letterToFirstWordMap[letter]
        textView?.let {
            val scrollToY = it.top
            scrollView.post {
                scrollView.smoothScrollTo(0, scrollToY)
            }
        }
    }


    // TODO: Figure out what this does
//    private fun onCharacterButtonClick(char: Char, songList: List<Audio>) {
//        val charLine = findViewById<LinearLayout>(R.id.charLine)
//
//        val iterator = songList.iterator()
//        var position = 0
//        while (iterator.hasNext()) {
//            val song = iterator.next()
//            val sanitizedSong = removePrefix(song.title)
//            val firstChar = sanitizedSong.trimStart().uppercase()[0]
//            if (firstChar == char) {
//                // Scroll to the first instance of the selected character
//                val view = charLine.getChildAt(position)
//                charLine.scrollTo(0, 0)
//                break
//            }
//            position++
//        }
//    }

    fun createSongButton(audio: Song): LinearLayout  {
        val compoundTextView = LinearLayout(this)
        compoundTextView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        compoundTextView.orientation = LinearLayout.VERTICAL
        compoundTextView.gravity = Gravity.CENTER

        val titleTextView = UI.createTextView(this, audio.getTitle(), true, compoundTextView)
        val artistTextView = UI.createTextView(this, audio.getArtist(), false, compoundTextView)

        val currentColor = ContextCompat.getColor(this, R.color.white)

        val colorPressed = ContextCompat.getColor(this, R.color.light_gray)

        compoundTextView.addView(titleTextView)
        compoundTextView.addView(artistTextView)

        val stateListDrawable = StateListDrawable()
        stateListDrawable.addState(intArrayOf(android.R.attr.state_pressed), ColorDrawable(colorPressed))
        stateListDrawable.addState(intArrayOf(android.R.attr.state_focused), ColorDrawable(colorPressed))
        stateListDrawable.addState(intArrayOf(android.R.attr.state_activated), ColorDrawable(colorPressed))
        stateListDrawable.addState(intArrayOf(), ColorDrawable(currentColor))

        compoundTextView.background = stateListDrawable

        val floatingTitle = findViewById<TextView>(R.id.titleTextView)
        val floatingArtist = findViewById<TextView>(R.id.artistTextView)
        val floatingButtons = findViewById<LinearLayout>(R.id.iconContainer)

        compoundTextView.setOnClickListener {
            floatingTitle.text = audio.getTitle()
            floatingArtist.text = audio.getArtist()

            floatingButtons.visibility = View.VISIBLE

            if(MusicPlayer.checkIfPlayerEmpty() != null) {
                MusicPlayer.stop()
                MusicPlayer.release()
            }

            MusicPlayer.createPlayer(this, audio.getUri())
            MusicPlayer.play()

            SongOrder.createDefaultOrder(generatedSongOrder)
            SongOrder.setCurrentOrder(SongOrder.getDefault().toMutableMap())
            SongOrder.setCurrentSong(audio)
//
//            Log.d("Song", "${SongOrder.getDefault()}")
//            Log.d("Song", "${SongOrder.getCurrentOrder()}")

//            val x = it.x
//            val y = it.y
//            Toast.makeText(this, "X: $x, y: $y", Toast.LENGTH_SHORT).show()
        }

//        compoundTextView.setOnLongClickListener() {
//            Toast.makeText(this, "Long", Toast.LENGTH_SHORT).show()
//            true
//        }

        val pauseButton: ImageView = findViewById(R.id.pauseButton)
        val playButton: ImageView = findViewById(R.id.playButton)
        val viewQButton: ImageView = findViewById(R.id.ViewQButton)

        pauseButton.setOnClickListener {
            MusicPlayer.pause()
            pauseButton.visibility = View.INVISIBLE
            playButton.visibility = View.VISIBLE
        }

        playButton.setOnClickListener {
            MusicPlayer.resume()
            playButton.visibility = View.INVISIBLE
            pauseButton.visibility = View.VISIBLE
        }

        viewQButton.setOnClickListener {
            val intent = Intent(this, SongOrderActivity::class.java)
            startActivity(intent)
        }


        return compoundTextView
    }

//    fun createTextView(text: String, isTitle: Boolean, parent: LinearLayout): TextView {
//        val textView = TextView(this)
//        textView.text = text
//        textView.maxLines = 1
//        textView.ellipsize = TextUtils.TruncateAt.END
//
//        // Customize font size and rotation based on whether it's a title or artist
//        textView.textSize = if (isTitle) 20f else 18f
//
//        //TODO: text slider if (text.length > parent.width)
////        textView.rotation = if (text.length > parent.width) 90f else 0f
//
//        return textView
//    }
//
//    fun createSeparator(): View {
//        val separator = View(this)
//        val layoutParams = LinearLayout.LayoutParams(
//            LinearLayout.LayoutParams.MATCH_PARENT,
//            resources.getDimensionPixelSize(R.dimen.separator_height)
//        )
//
//        separator.layoutParams = layoutParams
//        separator.setBackgroundColor(ContextCompat.getColor(this, R.color.black))
//
//        return separator
//    }

    private fun getAllAudioFiles(context: Context): List<Song> {
            //
        val audio = mutableListOf<Song>()

//        val db = NoSqlDB()
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

                audio.add(Song(
                        id, name, title,
                        artist, artistId, album, albumId, albumartist,
                        track, mime, isDownload, data, uri
                    )
                )
            }
        }
        return audio
    }

    fun errorMsg(text: String, context: Context) {

//        val rootLayout = findViewById<LinearLayout>(R.id.rootLayout)
        val rootLayout = LinearLayout(context)
        rootLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        rootLayout.orientation = LinearLayout.VERTICAL
        rootLayout.gravity = Gravity.CENTER
        // Set a solid color background (you can use Color.parseColor for hex colors)

        val textView = TextView(context)
        textView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        textView.text = text
        textView.gravity = Gravity.CENTER
        textView.setTextColor(Color.BLACK)

        textView.append("\n\nTo enable the permission, go to app settings.")
        textView.setOnClickListener {
            openAppSettings()
        }

        textView.setTextSize(24f)

        rootLayout.addView(textView)
        setContentView(rootLayout)
    }

    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    fun showBubbleText(anchorView: View, bubbleText: CharSequence) {

        val ovalShape = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.BLUE)
            setSize(150, 150) // Set your desired size
        }

        // Create a LinearLayout to hold the bubble text
        val bubbleLayout = LinearLayout(this)
        bubbleLayout.orientation = LinearLayout.VERTICAL
        bubbleLayout.background = ovalShape
        bubbleLayout.gravity = Gravity.CENTER
//        bubbleLayout.setBackgroundResource(R.drawable.ic_circle) // Customize bubble background


//        Toast.makeText(this,"bubble", Toast.LENGTH_SHORT).show()

        // Create a TextView for the bubble text
        val bubbleTextView = TextView(this)
        bubbleTextView.text = bubbleText
        bubbleTextView.textSize = 16f
        bubbleTextView.gravity = Gravity.CENTER
        bubbleTextView.setTextColor(ContextCompat.getColor(this, R.color.white)) // Customize text color
        bubbleTextView.setPadding(16, 8, 16, 8)

        // Add the TextView to the LinearLayout
        bubbleLayout.addView(bubbleTextView)

        // Create a PopupWindow with the bubble text layout
        val popupWindow = PopupWindow(
            bubbleLayout,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        // Show the PopupWindow below the anchor view
        popupWindow.showAsDropDown(anchorView, -275, -anchorView.height, Gravity.TOP)

        dismissPopupWindow = popupWindow

        bubbleLayout.setOnClickListener {
            popupWindow.dismiss()
        }

    }

    fun grabFromData() = runBlocking {
        val db = BackUpDB()
        val dbResults = db.getArtistAlbumSongs()

        val deferredResults = dbResults.map { artistLayer ->
            async {
                // For each artist, process their albums concurrently
                val albums = artistLayer.getList("albums", Document::class.java).orEmpty().flatMap { albumLayer ->
                    // For each album, process the songs asynchronously
                    val songs = albumLayer.getList("songs", Song::class.java).orEmpty()
                    songs
                }
                albums // Return the combined list of songs for this artist
            }
        }

        // Await the completion of all async tasks and collect the results
        val allSongs = deferredResults.awaitAll().flatten()

        allSongs
    }

}
