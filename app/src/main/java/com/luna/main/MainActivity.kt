package com.luna.main

import android.content.Intent
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import android.widget.ImageButton
import CharButtonAdapter
import android.content.Context
import android.graphics.Color
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat.startActivity

import com.luna.data.Song
import com.luna.global.SongOrder
import com.luna.utils.BackEnd
import com.luna.utils.UI
import com.luna.global.MusicPlayer
import com.luna.utils.QueryTable
import com.luna.utils.RecycleViewAdapter
import com.luna.utils.ButtonCreation


class MainActivity : AppCompatActivity() {

    private val query: QueryTable = QueryTable(this)
    private lateinit var songAdapter: RecycleViewAdapter<Song>
    private lateinit var charAdapter: CharButtonAdapter
    private lateinit var generatedSongList: List<Song>
    private val letterToFirstInstance: MutableMap<String, Int> = mutableMapOf()
    private lateinit var uniqueChars: MutableList<Char>

    private val buttonCreation = object : ButtonCreation(this) {}

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
                songAdapter = RecycleViewAdapter(this@MainActivity, generatedSongList) { song ->
                    buttonCreation.createSongButton(this@MainActivity, song)
                }
                songRecyclerView.adapter = songAdapter

                // Now pass the updated letterToFirstInstance to CharAdapter
                charAdapter = CharButtonAdapter(this@MainActivity, uniqueChars, letterToFirstInstance) { position ->
                    songRecyclerView.smoothScrollToPosition(position) // Scroll to song position
                }
                charRecyclerView.adapter = charAdapter

                searchButton.setOnClickListener {
                    if (!it.isEnabled) return@setOnClickListener
                    UI.setClickCooldown(it)
                    val intent = Intent(this@MainActivity, SearchAlgoActivity::class.java)
                    startActivity(intent)
                }

                val refreshButton: ImageButton = findViewById(R.id.refreshButton)

                refreshButton.setOnClickListener {
//                    refreshButton.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.light_gray))
                    if (!it.isEnabled) return@setOnClickListener
                    UI.setClickCooldown(it)
                    CoroutineScope(Dispatchers.IO).launch {
                        (application as StartUp).getAllAudioFiles(this@MainActivity)
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
//                            recreate()
                            songAdapter.updateAdapter(generatedSongList)
                            charAdapter.updateCharAdapter(uniqueChars, letterToFirstInstance)
                            Log.d("refresh", "Refreshed!")
                        }
                    }
                }

//                ToolBar.refresh(this@MainActivity)
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

}