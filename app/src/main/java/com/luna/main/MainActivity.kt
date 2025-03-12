package com.luna.main

import CharButtonAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
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
            val sortedAudioFiles = BackEnd.sort(audioFiles)
            val uniqueChars = BackEnd.createKnownAlphabet(sortedAudioFiles).toList()

            withContext(Dispatchers.Main) {
                // Initialize SongAdapter first so it updates letterToFirstInstance
                val songAdapter = SongButtonAdapter(this@MainActivity, sortedAudioFiles) { song ->
                    createSongButton(song)
                }
                songRecyclerView.adapter = songAdapter

                // Now pass the updated letterToFirstInstance to CharAdapter
                val charAdapter = CharButtonAdapter(this@MainActivity, uniqueChars, songAdapter.getDataset()) { position ->
                    songRecyclerView.smoothScrollToPosition(position) // Scroll to song position
                }
                charRecyclerView.adapter = charAdapter
            }
        }
    }

    fun createSongButton(audio: Song): LinearLayout  {
        val songButton = UI.createButton(this, audio)

        val floatingTitle = findViewById<TextView>(R.id.titleTextView)
        val floatingArtist = findViewById<TextView>(R.id.artistTextView)
        val floatingButtons = findViewById<LinearLayout>(R.id.iconContainer)

        songButton.setOnClickListener {
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


        return songButton
    }

}