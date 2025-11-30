package com.luna.main

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuInflater
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import androidx.appcompat.widget.SearchView
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
        //TODO: weird border problem investigate later, test case: "aa"
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

                                //TODO: Update if all instances of an artist, album, alnumartist have been deleted

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

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {

            val searchView = findViewById<SearchView>(R.id.searchBar)
            val searchEditText = searchView.findViewById<EditText>(
                androidx.appcompat.R.id.search_src_text
            )

            if (searchEditText != null && searchEditText.hasFocus()) {
                val outRect = Rect()
                searchEditText.getGlobalVisibleRect(outRect)

                if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    searchEditText.clearFocus()
                    hideKeyboard(searchEditText)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_engine)

        val searchView = findViewById<SearchView>(R.id.searchBar)
        searchView.isIconified = false
        searchView.queryHint = "Search"
        searchView.clearFocus()

        val songDisplayContainer = findViewById<LinearLayout>(R.id.songDisplayContainer)
        val artistDisplayContainer = findViewById<LinearLayout>(R.id.artistDisplayContainer)
        val albumDisplayContainer = findViewById<LinearLayout>(R.id.albumDisplayContainer)

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
                                songDisplayContainer.visibility = View.VISIBLE
                                displaySongs(sortedSongQuery)
                            }

                            if (artistQueryResult.isNotEmpty()) {
                                artistDisplayContainer.visibility = View.VISIBLE
                                displayArtists(sortedArtistQuery)
                            }

                            if (albumQueryResult.isNotEmpty()) {
                                albumDisplayContainer.visibility = View.VISIBLE
                                displayAlbum(sortedAlbumQuery)
                            }
                        }
                    }
                }

                searchView.clearFocus()

                return true
            }

            override fun onQueryTextChange(input: String?): Boolean {
                // Handle text change (e.g., filter a list dynamically)

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
                                songDisplayContainer.visibility = View.VISIBLE
                                displaySongs(sortedSongQuery)
                            } else {
                                songDisplayContainer.visibility = View.GONE
                            }

                            if (artistQueryResult.isNotEmpty()) {
                                artistDisplayContainer.visibility = View.VISIBLE
                                displayArtists(sortedArtistQuery)
                            } else {
                                artistDisplayContainer.visibility = View.GONE
                            }

                            if (albumQueryResult.isNotEmpty()) {
                                albumDisplayContainer.visibility = View.VISIBLE
                                displayAlbum(sortedAlbumQuery)
                            } else{
                                albumDisplayContainer.visibility = View.GONE
                            }
                        }
                    }
                } else {
                    songDisplayContainer.visibility = View.GONE
                    artistDisplayContainer.visibility = View.GONE
                    albumDisplayContainer.visibility = View.GONE
                }

                return true
            }
        })
    }

    //TODO: adding highlights of where the input is located will be held off till future, maybe.

    private fun displaySongs(sortedSongQuery: List<Song>) {
//        val songDisplayContainer = findViewById<LinearLayout>(R.id.songDisplayContainer)
        val songQueryTitle = findViewById<TextView>(R.id.songQueryTitle)
        val songRecyclerView: RecyclerView = findViewById(R.id.songRecyclerView)

        songAdapter = RecycleViewAdapter(this@SearchAlgoActivity, sortedSongQuery) { song ->
            buttonCreation.createSongButton(this@SearchAlgoActivity, song)
        }

        songRecyclerView.adapter = songAdapter

//        songDisplayContainer.visibility = View.VISIBLE
        songQueryTitle.text = "Songs (${sortedSongQuery.size})"
        songRecyclerView.layoutManager = LinearLayoutManager(this@SearchAlgoActivity)
        adjustRecyclerHeight(
            recycler = songRecyclerView,
            itemCount = sortedSongQuery.size,
            maxVisibleItems = 6,   // Song row height in dp
            maxHeightDp = 316    // Max height allowed (example)
        )
    }

    private fun displayArtists(sortedArtistQuery: List<String>) {
//        val artistDisplayContainer = findViewById<LinearLayout>(R.id.artistDisplayContainer)
        val artistQueryTitle = findViewById<TextView>(R.id.artistQueryTitle)
        val artistRecyclerView: RecyclerView = findViewById(R.id.artistRecyclerView)

        artistAdapter = RecycleViewAdapter(this@SearchAlgoActivity, sortedArtistQuery) { artist ->
            buttonCreation.createArtistButton(this@SearchAlgoActivity, artist)
        }
        artistRecyclerView.adapter = artistAdapter

//        artistDisplayContainer.visibility = View.VISIBLE
        artistQueryTitle.text = "Artists (${sortedArtistQuery.size})"
        artistRecyclerView.layoutManager = LinearLayoutManager(this@SearchAlgoActivity)
        adjustRecyclerHeight(
            recycler = artistRecyclerView,
            itemCount = sortedArtistQuery.size,
            maxVisibleItems = 8,   // Song row height in dp
            maxHeightDp = 228    // Max height allowed (example)
        )
    }

    private fun displayAlbum(sortedAlbumQuery: List<Pair<String,String>>) {
//        val albumDisplayContainer = findViewById<LinearLayout>(R.id.albumDisplayContainer)
        val albumQueryTitle = findViewById<TextView>(R.id.albumQueryTitle)
        val albumRecyclerView: RecyclerView = findViewById(R.id.albumRecyclerView)

        albumAdapter = RecycleViewAdapter(this@SearchAlgoActivity, sortedAlbumQuery) { album ->
            buttonCreation.createAlbumButton(this@SearchAlgoActivity, album)
        }
        albumRecyclerView.adapter = albumAdapter

//        albumDisplayContainer.visibility = View.VISIBLE
        albumQueryTitle.text = "Albums (${sortedAlbumQuery.size})"
        albumRecyclerView.layoutManager = LinearLayoutManager(this@SearchAlgoActivity)
        adjustRecyclerHeight(
            recycler = albumRecyclerView,
            itemCount = sortedAlbumQuery.size,
            maxVisibleItems = 4,   // Song row height in dp
            maxHeightDp = 211    // Max height allowed (example)
        )
    }

    //TODO: till I get a better solution

    private fun adjustRecyclerHeight(
        recycler: RecyclerView,
        itemCount: Int,
        maxVisibleItems: Int,
        maxHeightDp: Int
    ) {
        val itemHeightDp = (maxHeightDp / maxVisibleItems).toFloat()

        // Total height in dp
        val totalHeightDp = itemCount * itemHeightDp

        // Clamp to maxHeightDp
        val finalHeightDp = minOf(totalHeightDp, maxHeightDp.toFloat())

        // Convert final height to pixels
        val density = recycler.resources.displayMetrics.density
        recycler.layoutParams.height = (finalHeightDp * density).toInt()

        recycler.requestLayout()
    }


    //TODO: find cleaner way to hide keyboard
    private fun Activity.hideKeyboard(currentFocus: EditText) {
        val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus.windowToken, 0)
    }
}