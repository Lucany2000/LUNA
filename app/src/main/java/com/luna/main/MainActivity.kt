package com.luna.main

import CharButtonAdapter
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*


import com.luna.data.Song
import com.luna.global.SongOrder
import com.luna.utils.BackEnd
import com.luna.utils.UI
import com.luna.global.MusicPlayer
import com.luna.utils.QueryTable
import com.luna.utils.SongButtonAdapter


class MainActivity : AppCompatActivity() {

    private val query: QueryTable = QueryTable(this)
    private lateinit var songAdapter: SongButtonAdapter
    private lateinit var charAdapter: CharButtonAdapter
    private lateinit var generatedSongList: List<Song>
    private val letterToFirstInstance: MutableMap<String, Int> = mutableMapOf()
    private lateinit var uniqueChars: MutableList<Char>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val charRecyclerView: RecyclerView = findViewById(R.id.charRecyclerView)
        val songRecyclerView: RecyclerView = findViewById(R.id.recyclerView)

        charRecyclerView.layoutManager = LinearLayoutManager(this)
        songRecyclerView.layoutManager = LinearLayoutManager(this)

        CoroutineScope(Dispatchers.IO).launch {
            val readOnlyDB = query.readOnlyMode()
            val audioFiles = query.getSongs(readOnlyDB)
            generatedSongList = BackEnd.sort(audioFiles)
            uniqueChars = BackEnd.createKnownAlphabet(generatedSongList).toMutableList()

            generatedSongList.forEachIndexed { index, song ->
                val title = song.getTitle()
                val firstChar = BackEnd.removePrefix(title).firstOrNull()?.uppercase()

                if (firstChar != null && !letterToFirstInstance.containsKey(firstChar)) {
                    letterToFirstInstance[firstChar] = index
                }
            }

            withContext(Dispatchers.Main) {
                // Initialize SongAdapter first so it updates letterToFirstInstance
                songAdapter = SongButtonAdapter(this@MainActivity, generatedSongList) { song ->
                    createSongButton(song)
                }
                songRecyclerView.adapter = songAdapter

                // Now pass the updated letterToFirstInstance to CharAdapter
                charAdapter = CharButtonAdapter(this@MainActivity, uniqueChars, letterToFirstInstance) { position ->
                    songRecyclerView.smoothScrollToPosition(position) // Scroll to song position
                }
                charRecyclerView.adapter = charAdapter
            }
        }
    }

    fun createSongButton(audio: Song): LinearLayout  {
        val songButton = UI.createButton(this, audio)

        val threeDotImageView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT).apply { // Circle size
                gravity = Gravity.END // Align to right
            }
            val currentColor = ContextCompat.getColor(context, R.color.white)
            val colorPressed = ContextCompat.getColor(context, R.color.light_gray)

            //TODO: Figure out how to have a working oval background for the button
            val ovalDrawable = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(currentColor) // Default oval background color
                setStroke(4, Color.BLACK) // Set border color and width
                setSize(width + 24, height + 24) // Adjust size around the image
            }

            val stateListDrawable = StateListDrawable().apply {
                addState(intArrayOf(android.R.attr.state_pressed), ColorDrawable(colorPressed)) // When pressed
                addState(intArrayOf(), ColorDrawable(currentColor)) // Default state (unpressed)
            }

            background = stateListDrawable // Set background to state list drawable

            scaleType = ImageView.ScaleType.CENTER
            setPadding(12, 12, 12, 12) // Padding inside the circle
            setImageResource(R.drawable.three_dot_menu) // Your 3-dot vector image


            setOnClickListener {
                val popupMenu = PopupMenu(context, this)

                // Inflate the menu from XML resource (you can define your own menu XML)
                MenuInflater(context).inflate(R.menu.popup_menu, popupMenu.menu)

                // Set menu item click listeners
                popupMenu.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.delete -> {

                            CoroutineScope(Dispatchers.IO).launch {
                                val writeToDB = query.writeMode()
                                query.blacklist(writeToDB, audio)

                                val position = generatedSongList.indexOf(audio)
                                generatedSongList.toMutableList().removeAt(position)

                                val targetChar = BackEnd.removePrefix(audio.getTitle()).firstOrNull()?.uppercase()

                                letterToFirstInstance.remove(targetChar)
                                if (letterToFirstInstance[targetChar] == position) {
                                    generatedSongList.forEachIndexed { index, song ->
                                        val title = song.getTitle()
                                        val firstChar = BackEnd.removePrefix(title).firstOrNull()?.uppercase().toString()

                                        if (firstChar == targetChar && !letterToFirstInstance.containsKey(firstChar)) {
                                            letterToFirstInstance[firstChar] = index
                                        }
                                    }
                                }

                                if (!letterToFirstInstance.containsKey(targetChar)) {
                                    uniqueChars.remove(targetChar?.first())
                                }

                                withContext(Dispatchers.Main) {
                                    songAdapter.notifyItemRemoved(position)
                                    charAdapter.updateCharAdapter(
                                        uniqueChars,
                                        letterToFirstInstance
                                    )
                                }
                            }

                            true
                        }
                        R.id.next -> {
                            // Handle option 2 click
                            Log.d("PopUp", "${audio.getTitle()} will play next")
                            true
                        }
                        R.id.queue -> {
                            // Handle option 2 click
                            Log.d("PopUp", "${audio.getTitle()} has been added to the queue")
                            true
                        }
                        else -> false
                    }
                }

                // Show the menu
                popupMenu.show()
            }
        }

        // Set up the container for song button and 3-dot menu icon
        val songButtonContainer = LinearLayout(this)
        songButtonContainer.orientation = LinearLayout.HORIZONTAL
        songButtonContainer.gravity = Gravity.CENTER_VERTICAL

        val songButtonParams = LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f // Song button takes available space
        )
        songButton.layoutParams = songButtonParams

//        val threeDotImageParams = LinearLayout.LayoutParams(
//            LinearLayout.LayoutParams.WRAP_CONTENT,
//            LinearLayout.LayoutParams.WRAP_CONTENT
//        )
//        threeDotImageParams.gravity = Gravity.END // Align 3-dot menu to the right side
//        threeDotImageView.layoutParams = threeDotImageParams


        // Add songButton and dotImageView to the container
        songButtonContainer.addView(songButton)
        songButtonContainer.addView(threeDotImageView)


        songButton.setOnClickListener {

            val floatingTitle = findViewById<TextView>(R.id.titleTextView)
            val floatingArtist = findViewById<TextView>(R.id.artistTextView)
            val floatingButtons = findViewById<LinearLayout>(R.id.iconContainer)

            floatingTitle.text = audio.getTitle()
            floatingArtist.text = audio.getArtist()

            floatingButtons.visibility = View.VISIBLE

            if(MusicPlayer.checkIfPlayerEmpty() != null) {
                MusicPlayer.stop()
                MusicPlayer.release()
            }

            MusicPlayer.createPlayer(this, audio.getUri())
            MusicPlayer.play()

            val readOnlyDB = query.readOnlyMode()

            val audioFiles = query.getSongs(readOnlyDB)

            SongOrder.createDefaultOrder(audioFiles)
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


        return songButtonContainer
    }

}