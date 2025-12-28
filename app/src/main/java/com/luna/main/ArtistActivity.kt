package com.luna.main

import CharButtonAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.luna.data.Song
import com.luna.utils.BackEnd
import com.luna.utils.ButtonCreation
import com.luna.utils.QueryTable
import com.luna.utils.RecycleViewAdapter
import com.luna.utils.UI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ArtistActivity : AppCompatActivity() {
    private val query: QueryTable = QueryTable(this)
    private lateinit var artistAdapter: RecycleViewAdapter<String>
    private lateinit var charAdapter: CharButtonAdapter
    private lateinit var generatedArtistList: List<String>
    private val letterToFirstInstance: MutableMap<String, Int> = mutableMapOf()
    private lateinit var uniqueChars: MutableList<Char>

    private val buttonCreation = object : ButtonCreation(this) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artist)

        val navBar: TabLayout = findViewById(R.id.NavBar)
        val tracksTab = navBar.getTabAt(2)
        tracksTab?.select()

        val charRecyclerView: RecyclerView = findViewById(R.id.charRecyclerView)
        val artistRecyclerView: RecyclerView = findViewById(R.id.recyclerView)

        val searchButton: ImageButton = findViewById(R.id.searchButton)

        charRecyclerView.layoutManager = LinearLayoutManager(this)
        artistRecyclerView.layoutManager = LinearLayoutManager(this)

        CoroutineScope(Dispatchers.IO).launch {
            val readOnlyDB = query.readOnlyMode()
            val audioFiles = query.getArtists(readOnlyDB)
            generatedArtistList = BackEnd.sort(audioFiles)
            uniqueChars = BackEnd.createKnownAlphabet(generatedArtistList).toMutableList()

            generatedArtistList.forEachIndexed { index, artist ->
                val title = artist
                val firstChar = BackEnd.removePrefix(title).firstOrNull()?.uppercase()

                if (firstChar != null && !letterToFirstInstance.containsKey(firstChar)) {
                    letterToFirstInstance[firstChar] = index
                }
            }

            withContext(Dispatchers.Main) {
                // Initialize artistAdapter first so it updates letterToFirstInstance
                artistAdapter = RecycleViewAdapter(this@ArtistActivity, generatedArtistList) { artist ->
                    buttonCreation.createArtistButton(this@ArtistActivity, artist)
                }
                artistRecyclerView.adapter = artistAdapter

                // Now pass the updated letterToFirstInstance to CharAdapter
                charAdapter = CharButtonAdapter(this@ArtistActivity, uniqueChars, letterToFirstInstance) { position ->
                    artistRecyclerView.smoothScrollToPosition(position) // Scroll to song position
                }
                charRecyclerView.adapter = charAdapter

                searchButton.setOnClickListener {
                    if (!it.isEnabled) return@setOnClickListener
                    UI.setClickCooldown(it)
                    val intent = Intent(this@ArtistActivity, SearchAlgoActivity::class.java)
                    startActivity(intent)
                }

                val refreshButton: ImageButton = findViewById(R.id.refreshButton)

                refreshButton.setOnClickListener {
//                    refreshButton.setBackgroundColor(ContextCompat.getColor(this@ArtistActivity, R.color.light_gray))
                    if (!it.isEnabled) return@setOnClickListener
                    UI.setClickCooldown(it)
                    CoroutineScope(Dispatchers.IO).launch {
                        (application as StartUp).getAllAudioFiles(this@ArtistActivity)
                        val readOnlyDB = query.readOnlyMode()
                        val audioFiles = query.getArtists(readOnlyDB)
                        generatedArtistList = BackEnd.sort(audioFiles)
                        uniqueChars = BackEnd.createKnownAlphabet(generatedArtistList).toMutableList()

                        generatedArtistList.forEachIndexed { index, artist ->
                            val title = artist
                            val firstChar = BackEnd.removePrefix(title).firstOrNull()?.uppercase()

                            if (firstChar != null && !letterToFirstInstance.containsKey(firstChar)) {
                                letterToFirstInstance[firstChar] = index
                            }
                        }

                        withContext(Dispatchers.Main) {
//                            recreate()
                            artistAdapter.updateAdapter(generatedArtistList)
                            charAdapter.updateCharAdapter(uniqueChars, letterToFirstInstance)
                            Log.d("refresh", "Refreshed!")
                        }
                    }
                }

                navBar.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab?) {
                        when (tab?.position) {
                            0 -> {
                                val intent = Intent(this@ArtistActivity, MainActivity::class.java)
                                startActivity(intent)
                            }
                            1 -> {
                                val intent = Intent(this@ArtistActivity, AlbumActivity::class.java)
                                startActivity(intent)
                            }
                            2 -> {}
                        }
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab?) {}
                    override fun onTabReselected(tab: TabLayout.Tab?) {}
                })

//                ToolBar.refresh(this@ArtistActivity)
//                refresh()

                //TODO: add Settings later
                /*
                val settingsButton: ImageButton = findViewById(R.id.settingsButton)
                settingsButton.setOnClickListener {
                    val intent = Intent(this@ArtistActivity, SettingsActivity::class.java)
                    startActivity(intent)
                }
                */

            }
        }
    }
}