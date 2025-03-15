package com.luna.main

import android.content.Intent
import android.graphics.drawable.GradientDrawable
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
import android.app.Activity
import android.widget.ImageButton

import com.luna.data.Song
import com.luna.global.SongOrder
import com.luna.utils.BackEnd
import com.luna.utils.UI
import com.luna.global.MusicPlayer
import com.luna.utils.QueryTable
import com.luna.utils.SongButtonAdapter
import CharButtonAdapter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.luna.global.ToolBar



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

        val searchButton: ImageButton = findViewById(R.id.searchButton)

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

                searchButton.setOnClickListener {
//                    val intent = Intent(this@MainActivity, SearchAlgoActivity::class.java)
//                    startActivity(intent)
                }

                ToolBar.refresh(this@MainActivity)
//                refresh()

                //TODO: add Settings later
                /*
                val settingsButton: ImageButton = findViewById(R.id.settingsButton)
                settingsButton.setOnClickListener {
                    val intent = Intent(this@MainActivity, SettingsActivity::class.java)
                    startActivity(intent)
                }
                */

            }
        }
    }

    fun createSongButton(audio: Song): LinearLayout  {
        val songButton = UI.createButton(this, audio)

        val threeDotBackground = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT).apply { // Circle size
                gravity = Gravity.END
                setBackgroundColor(Color.WHITE)
                }
        }

        val threeDotImageView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT).apply { // Circle size
                gravity = Gravity.CENTER // Align to right
            }

//            val currentColor = ContextCompat.getColor(context, R.color.white)
//            val colorPressed = ContextCompat.getColor(context, R.color.light_gray)
//
//            val ovalDefault = GradientDrawable().apply {
//                shape = GradientDrawable.OVAL
//                setColor(currentColor) // Default oval background color
//                setSize(12, 12) // Adjust size around the image
//            }
//
//            val ovalPressed = GradientDrawable().apply {
//                shape = GradientDrawable.OVAL
//                setColor(colorPressed) // Default oval background color
//                setSize(12, 12)
//            }
//
//            val stateListDrawable = StateListDrawable().apply {
//                addState(intArrayOf(android.R.attr.state_pressed), ovalPressed)
//                addState(intArrayOf(android.R.attr.state_focused), ovalPressed)
//                addState(intArrayOf(), ovalDefault) // Default state (unpressed)
//            }
//
//            background = stateListDrawable // Set background to state list drawable

            setBackgroundResource(R.drawable.three_dot_selector)

            setImageResource(R.drawable.three_dot_icon) // Your 3-dot vector image


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

        // Add songButton and dotImageView to the container
        threeDotBackground.addView(threeDotImageView)
        songButtonContainer.addView(songButton)
        songButtonContainer.addView(threeDotBackground)


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

    fun refresh(activity: Activity = this) {
        val refreshButton: ImageButton = activity.findViewById(R.id.refreshButton)

        refreshButton.setOnClickListener {
            refreshButton.setBackgroundColor(ContextCompat.getColor(activity, R.color.light_gray))
            CoroutineScope(Dispatchers.IO).launch {
                (activity.application as StartUp).getAllAudioFiles(activity)

                withContext(Dispatchers.Main) {
                    activity.recreate()
                }
            }
        }
    }

}