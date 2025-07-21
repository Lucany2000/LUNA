package com.luna.main

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import com.luna.data.Song
import com.luna.global.MusicPlayer
import com.luna.global.SongOrder
import com.luna.utils.BackEnd
import com.luna.utils.QueryTable
import com.luna.utils.RecycleViewAdapter
import com.luna.utils.ButtonCreation

class SearchAlgoActivity: AppCompatActivity() {

    private lateinit var songAdapter: RecycleViewAdapter<Song>
    private lateinit var artistAdapter: RecycleViewAdapter<String>
    private lateinit var albumAdapter: RecycleViewAdapter<Pair<String,String>>
    private val query = QueryTable(this)
    private lateinit var sortedSongQuery: List<Song>

    private val buttonCreation = object : ButtonCreation(this) {
        override fun createSongButton(activity: AppCompatActivity, audio: Song): LinearLayout {
            val songButton = createButton(activity, audio)

            val threeDotBackground = LinearLayout(activity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT).apply { // Circle size
                    gravity = Gravity.END
                    setBackgroundColor(Color.WHITE)
                }
            }

            val threeDotImageView = ImageView(activity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT).apply { // Circle size
                    gravity = Gravity.CENTER // Align to right
                }

                setBackgroundResource(R.drawable.three_dot_selector)

                setImageResource(R.drawable.three_dot_icon) // Your 3-dot vector image


                setOnClickListener {
//                if (!it.isEnabled) return@setOnClickListener
//                UI.setClickCooldown(it)
                    val popupMenu = PopupMenu(context, this)

                    // Inflate the menu from XML resource (you can define your own menu XML)
                    MenuInflater(context).inflate(R.menu.popup_menu, popupMenu.menu)

                    // Set menu item click listeners
                    popupMenu.setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.delete -> {
//                            if (!item.isEnabled) return@setOnMenuItemClickListener true
//                            item.isEnabled = false
//                            Handler(Looper.getMainLooper()).postDelayed({ item.isEnabled = true }, 1000)

                                CoroutineScope(Dispatchers.IO).launch {
                                    val writeToDB = query.writeMode()
                                    query.blacklist(writeToDB, audio)

                                    val position = sortedSongQuery.indexOf(audio)
                                    sortedSongQuery.toMutableList().removeAt(position)

                                    withContext(Dispatchers.Main) {
                                        songAdapter.notifyItemRemoved(position)
                                    }
                                }

                                true
                            }
                            R.id.next -> {
//                            if (!item.isEnabled) return@setOnMenuItemClickListener true
//                            item.isEnabled = false
//                            Handler(Looper.getMainLooper()).postDelayed({ item.isEnabled = true }, 1000)

                                Log.d("PopUp", "${audio.getTitle()} will play next")
                                true
                            }
                            R.id.queue -> {
//                            if (!item.isEnabled) return@setOnMenuItemClickListener true
//                            item.isEnabled = false
//                            Handler(Looper.getMainLooper()).postDelayed({ item.isEnabled = true }, 1000)

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
            val songButtonContainer = LinearLayout(activity)
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

            val pauseButton: ImageView = activity.findViewById(R.id.pauseButton)
            val playButton: ImageView = activity.findViewById(R.id.playButton)
            val viewQButton: ImageView = activity.findViewById(R.id.ViewQButton)


            songButton.setOnClickListener {
//            if (!it.isEnabled) return@setOnClickListener
//            UI.setClickCooldown(it)
                val floatingTitle = activity.findViewById<TextView>(R.id.titleTextView)
                val floatingArtist = activity.findViewById<TextView>(R.id.artistTextView)
                val floatingButtons = activity.findViewById<LinearLayout>(R.id.iconContainer)

                floatingTitle.text = audio.getTitle()
                floatingArtist.text = audio.getArtist()

                floatingButtons.visibility = View.VISIBLE

                if(MusicPlayer.checkIfPlayerEmpty() != null) {
                    if (!MusicPlayer.isPlaying()) {
                        playButton.visibility = View.INVISIBLE
                        pauseButton.visibility = View.VISIBLE
                    }
                    MusicPlayer.stop()
                    MusicPlayer.release()
                }

                MusicPlayer.createPlayer(activity, audio.getUri())
                MusicPlayer.play()

                SongOrder.createDefaultOrder(listOf(audio))
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

            pauseButton.setOnClickListener {
//            if (!it.isEnabled) return@setOnClickListener
//            UI.setClickCooldown(it)
                MusicPlayer.pause()
                pauseButton.visibility = View.INVISIBLE
                playButton.visibility = View.VISIBLE
            }

            playButton.setOnClickListener {
//            if (!it.isEnabled) return@setOnClickListener
//            UI.setClickCooldown(it)
                MusicPlayer.resume()
                playButton.visibility = View.INVISIBLE
                pauseButton.visibility = View.VISIBLE
            }

            viewQButton.setOnClickListener {
//            if (!it.isEnabled) return@setOnClickListener
//            UI.setClickCooldown(it)
                val intent = Intent(activity, SongOrderActivity::class.java)
                activity.startActivity(intent)
            }


            return songButtonContainer
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_engine)

        val searchView = findViewById<SearchView>(R.id.searchBar)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(input: String?): Boolean {
                // Handle search submission (e.g., API call, database query)
                //Testing Purposes
                if (!input.isNullOrBlank()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val readOnlyDB = query.readOnlyMode()
                        val queryResult = query.searchAlgo(readOnlyDB, input)
                        val songQueryResult = queryResult["song"]?.filterIsInstance<Song>() ?: emptyList()
                        val artistQueryResult = queryResult["artist"]?.filterIsInstance<String>() ?: emptyList()
                        val albumQueryResult = queryResult["album"]?.filterIsInstance<Pair<String, String>>() ?: emptyList()

                        sortedSongQuery = BackEnd.sort(songQueryResult)
                        val sortedArtistQuery = BackEnd.sort(artistQueryResult)
                        val sortedAlbumQuery = BackEnd.sort(albumQueryResult)

                        withContext(Dispatchers.Main){
                            if (songQueryResult.isNotEmpty()) {
                                displaySongs(sortedSongQuery)
                            }

                            if (artistQueryResult.isNotEmpty()) {
                                displayArtists(sortedArtistQuery)
                            }

                            if (albumQueryResult.isNotEmpty()) {
                                displayAlbum(sortedAlbumQuery)
                            }
                        }

//                        if (songQueryResult.isNotEmpty()) {
//                            sortedSongQuery = BackEnd.sort(songQueryResult)
//                            val songDisplayContainer = findViewById<LinearLayout>(R.id.songDisplayContainer)
//                            val songQueryTitle = findViewById<TextView>(R.id.songQueryTitle)
//                            val songRecyclerView: RecyclerView = findViewById(R.id.songRecyclerView)
//
//                            songAdapter = RecycleViewAdapter(this@SearchAlgoActivity, sortedSongQuery) { song ->
//                                buttonCreation.createSongButton(this@SearchAlgoActivity, song)
//                            }
//
//                            songRecyclerView.adapter = songAdapter
//
//                            withContext(Dispatchers.Main) {
//                                songDisplayContainer.visibility = View.VISIBLE
//                                songQueryTitle.text = "Songs (${sortedSongQuery.size})"
//                                songRecyclerView.layoutManager = LinearLayoutManager(this@SearchAlgoActivity)
//                            }
//
//                        }
//
//                        if (artistQueryResult.isNotEmpty()) {
//                            val sortedArtistQuery = BackEnd.sort(artistQueryResult)
//                            val artistDisplayContainer = findViewById<LinearLayout>(R.id.artistDisplayContainer)
//                            val artistQueryTitle = findViewById<TextView>(R.id.artistQueryTitle)
//                            val artistRecyclerView: RecyclerView = findViewById(R.id.artistRecyclerView)
//
//                            artistAdapter = RecycleViewAdapter(this@SearchAlgoActivity, sortedArtistQuery) { artist ->
//                                buttonCreation.createArtistButton(this@SearchAlgoActivity, artist)
//                            }
//                            artistRecyclerView.adapter = artistAdapter
//
//                            withContext(Dispatchers.Main) {
//                                artistDisplayContainer.visibility = View.VISIBLE
//                                artistQueryTitle.text = "Artists (${sortedArtistQuery.size})"
//                                artistRecyclerView.layoutManager = LinearLayoutManager(this@SearchAlgoActivity)
//                            }
//
//                        }
//
//                        if (albumQueryResult.isNotEmpty()) {
//                            val sortedAlbumQuery = BackEnd.sort(albumQueryResult)
//                            val albumDisplayContainer = findViewById<LinearLayout>(R.id.albumDisplayContainer)
//                            val albumQueryTitle = findViewById<TextView>(R.id.albumQueryTitle)
//                            val albumRecyclerView: RecyclerView = findViewById(R.id.albumRecyclerView)
//
//                            albumAdapter = RecycleViewAdapter(this@SearchAlgoActivity, sortedAlbumQuery) { album ->
//                                buttonCreation.createAlbumButton(this@SearchAlgoActivity, album)
//                            }
//                            albumRecyclerView.adapter = albumAdapter
//
//                            withContext(Dispatchers.Main) {
//                                albumDisplayContainer.visibility = View.VISIBLE
//                                albumQueryTitle.text = "Albums (${sortedAlbumQuery.size})"
//                                albumRecyclerView.layoutManager = LinearLayoutManager(this@SearchAlgoActivity)
//                            }
//
//                        }
                    }
                }

                return true
            }

            override fun onQueryTextChange(input: String?): Boolean {
                // Handle text change (e.g., filter a list dynamically)

//                if (!input.isNullOrBlank()) {
//                    CoroutineScope(Dispatchers.IO).launch {
//                        val readOnlyDB = query.readOnlyMode()
//                        val queryResult = query.searchAlgo(readOnlyDB, input)
//                        val songQueryResult = queryResult["song"]?.filterIsInstance<Song>() ?: emptyList()
//                        val artistQueryResult = queryResult["artist"]?.filterIsInstance<String>() ?: emptyList()
//                        val albumQueryResult = queryResult["album"]?.filterIsInstance<Pair<String, String>>() ?: emptyList()
//
//                        sortedSongQuery = BackEnd.sort(songQueryResult)
//                        val sortedArtistQuery = BackEnd.sort(artistQueryResult)
//                        val sortedAlbumQuery = BackEnd.sort(albumQueryResult)
//
//                        withContext(Dispatchers.Main){
//                            if (songQueryResult.isNotEmpty()) {
//                                displaySongs(sortedSongQuery)
//                            }
//
//                            if (artistQueryResult.isNotEmpty()) {
//                                displayArtists(sortedArtistQuery)
//                            }
//
//                            if (albumQueryResult.isNotEmpty()) {
//                                displayAlbum(sortedAlbumQuery)
//                            }
//                        }
//                    }
//                }

                return true
            }
        })

    }

    private fun displaySongs(sortedSongQuery: List<Song>) {
        val songDisplayContainer = findViewById<LinearLayout>(R.id.songDisplayContainer)
        val songQueryTitle = findViewById<TextView>(R.id.songQueryTitle)
        val songRecyclerView: RecyclerView = findViewById(R.id.songRecyclerView)

        songAdapter = RecycleViewAdapter(this@SearchAlgoActivity, sortedSongQuery) { song ->
            buttonCreation.createSongButton(this@SearchAlgoActivity, song)
        }

        songRecyclerView.adapter = songAdapter

        songDisplayContainer.visibility = View.VISIBLE
        songQueryTitle.text = "Songs (${sortedSongQuery.size})"
        songRecyclerView.layoutManager = LinearLayoutManager(this@SearchAlgoActivity)
    }

    private fun displayArtists(sortedArtistQuery: List<String>) {
        val artistDisplayContainer = findViewById<LinearLayout>(R.id.artistDisplayContainer)
        val artistQueryTitle = findViewById<TextView>(R.id.artistQueryTitle)
        val artistRecyclerView: RecyclerView = findViewById(R.id.artistRecyclerView)

        artistAdapter = RecycleViewAdapter(this@SearchAlgoActivity, sortedArtistQuery) { artist ->
            buttonCreation.createArtistButton(this@SearchAlgoActivity, artist)
        }
        artistRecyclerView.adapter = artistAdapter

        artistDisplayContainer.visibility = View.VISIBLE
        artistQueryTitle.text = "Artists (${sortedArtistQuery.size})"
        artistRecyclerView.layoutManager = LinearLayoutManager(this@SearchAlgoActivity)

    }

    private fun displayAlbum(sortedAlbumQuery: List<Pair<String,String>>) {
        val albumDisplayContainer = findViewById<LinearLayout>(R.id.albumDisplayContainer)
        val albumQueryTitle = findViewById<TextView>(R.id.albumQueryTitle)
        val albumRecyclerView: RecyclerView = findViewById(R.id.albumRecyclerView)

        albumAdapter = RecycleViewAdapter(this@SearchAlgoActivity, sortedAlbumQuery) { album ->
            buttonCreation.createAlbumButton(this@SearchAlgoActivity, album)
        }
        albumRecyclerView.adapter = albumAdapter

        albumDisplayContainer.visibility = View.VISIBLE
        albumQueryTitle.text = "Albums (${sortedAlbumQuery.size})"
        albumRecyclerView.layoutManager = LinearLayoutManager(this@SearchAlgoActivity)
    }
}