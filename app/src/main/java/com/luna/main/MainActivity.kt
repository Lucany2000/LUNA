package com.luna.main

import android.graphics.drawable.Drawable
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
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.provider.Settings
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ScrollView
import android.widget.Toast
import androidx.core.content.ContextCompat
import java.util.TreeSet


class MainActivity : AppCompatActivity() {

    private val REQUEST_PERMISSION_CODE = 0
    private var dismissPopupWindow: PopupWindow? = null

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
               errorMsg("No Audio Found")
            }
        }
    }

    private fun main() {

        val rootLayout = findViewById<LinearLayout>(R.id.rootLayout)

        val audioFiles = getAllAudioFiles(this).distinctBy { listOf(it.title,it.artist, it.album) }
        val sortedAudioFiles = sort(audioFiles)

        val charLine = findViewById<LinearLayout>(R.id.charLine)

        val uniqueChars = createKnownAlphabet(sortedAudioFiles)


        for (char in uniqueChars) {
            val button = TextView(this)
            button.text = char.toString()
            button.width = 96
            button.height = 96

            button.gravity = Gravity.CENTER
            button.textSize = 16f


            val currentColor = ContextCompat.getColor(this, R.color.light_gray)

            val colorPressed = Color.BLUE

            button.background = ColorDrawable(currentColor)


            button.setOnTouchListener { view, motionEvent ->
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {

                        button.background = ColorDrawable(colorPressed)

                        showBubbleText(view, button.text)

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


//            button.setOnClickListener {view ->
////                onCharacterButtonClick(char, sortedAudioFiles)
////                rootLayout.scrollTo(0, 25222)
//                showBubbleText(view, button.text)
//                Toast.makeText(this, button.text, Toast.LENGTH_SHORT).show()
//            }
//
//            button.setOnLongClickListener {view ->
////                onCharacterButtonClick(char, sortedAudioFiles)
////                rootLayout.scrollTo(0, 25222)
//                showBubbleText(view, button.text)
//                Toast.makeText(this, button.text, Toast.LENGTH_SHORT).show()
//                true
//            }

            // Add the button to the LinearLayout
            charLine.addView(button)
        }

        for (song in sortedAudioFiles) {
            val button = createSongButton(song)
            rootLayout.addView(button)

            val separator = createSeparator()
            rootLayout.addView(separator)
        }
    }

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

    fun createSongButton(audio: Audio): LinearLayout  {
        val compoundTextView = LinearLayout(this)
        compoundTextView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        compoundTextView.orientation = LinearLayout.VERTICAL
        compoundTextView.gravity = Gravity.CENTER

        val titleTextView = createTextView(audio.title, true, compoundTextView)
        val artistTextView = createTextView(audio.artist, false, compoundTextView)

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

        compoundTextView.setOnClickListener {
            floatingTitle.text = audio.title
            floatingArtist.text = audio.artist
            val x = it.x
            val y = it.y
            Toast.makeText(this, "X: $x, y: $y", Toast.LENGTH_SHORT).show()
        }

        compoundTextView.setOnLongClickListener() {
            Toast.makeText(this, "Long", Toast.LENGTH_SHORT).show()
            true
        }

        return compoundTextView
    }

    fun createSeparator(): View {
        val separator = View(this)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            resources.getDimensionPixelSize(R.dimen.separator_height)
        )

        separator.layoutParams = layoutParams
        separator.setBackgroundColor(ContextCompat.getColor(this, R.color.black))

        return separator
    }

    fun createTextView(text: String, isTitle: Boolean, parent: LinearLayout): TextView {
        val textView = TextView(this)
        textView.text = text
        textView.maxLines = 1
        textView.ellipsize = TextUtils.TruncateAt.END

        // Customize font size and rotation based on whether it's a title or artist
        textView.textSize = if (isTitle) 20f else 18f

        //TODO: text slider if (text.length > parent.width)
//        textView.rotation = if (text.length > parent.width) 90f else 0f

        return textView
    }


    fun errorMsg(text: String) {

//        val rootLayout = findViewById<LinearLayout>(R.id.rootLayout)
        val rootLayout = LinearLayout(this)
        rootLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        rootLayout.orientation = LinearLayout.VERTICAL
        rootLayout.gravity = Gravity.CENTER
        // Set a solid color background (you can use Color.parseColor for hex colors)

        val textView = TextView(this)
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


    private fun getAllAudioFiles(context: Context): List<Audio> {

        val audio = mutableListOf<Audio>()

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
                val artist = it.getString(artistColumn)
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
                audio.add(
                    Audio(
                        id, name, title,
                        artist, artistId, album, albumId, albumartist,
                        track, mime, isDownload, data, uri
                    )
                )
            }
        }
        return audio
    }

    fun sort(audioFiles: List<Audio>): List<Audio> {
        val customComparator = Comparator<Audio> { audio1, audio2 ->
            val title1 = audio1.title ?: ""
            val title2 = audio2.title ?: ""

            // Ignore case and handle 'A' and 'The' cases
            val title1WithoutPrefix = removePrefix(title1)
            val title2WithoutPrefix = removePrefix(title2)

            // Compare the titles without 'A' or 'The'
            title1WithoutPrefix.compareTo(title2WithoutPrefix, ignoreCase = true)
        }

        val sortedAudioFiles = audioFiles.sortedWith(customComparator)
        return sortedAudioFiles
    }

    fun removePrefix(title: String): String {
        val lowerCaseTitle = title.lowercase()
        return when {
            lowerCaseTitle.startsWith("the ") -> title.substring(4)
            lowerCaseTitle.startsWith("a ") -> title.substring(2)
            else -> title
        }
    }

    data class Audio(
        val id: Long,
        val name: String,
        val title: String,
        val artist: String,
        val artistId: Long,
        val album: String,
        val albumId: Long,
        val albumartist: String,
        val track: Long,
        val mime: String,
        val isDownload: Long,
        val data: String,
        val uri: Uri
    )
    fun createKnownAlphabet(songList: List<Audio>): TreeSet<Char> {
        val uniqueChars = TreeSet<Char>()

        // Iterate through songs to get unique first characters
        for (song in songList) {
            val sanitizedSong = removePrefix(song.title)
            val firstChar = sanitizedSong.trimStart().uppercase()[0]
            uniqueChars.add(firstChar)
        }
        return uniqueChars

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

}
