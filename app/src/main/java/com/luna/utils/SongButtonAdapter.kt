package com.luna.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import com.luna.data.Song
import com.luna.main.R

class SongButtonAdapter(
    private val context: Context,
    private val songs: List<Song>,
    private val createSongButton: (Song) -> LinearLayout): RecyclerView.Adapter<SongButtonAdapter.SongButtonViewHolder>() {

    class SongButtonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val songButtonContainer: LinearLayout = view.findViewById(R.id.mainButtonContainer)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongButtonViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.main_button, parent, false)
        return SongButtonViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongButtonViewHolder, position: Int) {
        val song = songs[position]

        // Create song button dynamically
        val songButton = createSongButton(song)
        holder.songButtonContainer.removeAllViews() // Clear previous view if exists
        holder.songButtonContainer.addView(songButton)
    }

    override fun getItemCount() = songs.size

    fun getDataset(): Map<String, Int> {
        val letterToFirstInstance = mutableMapOf<String, Int>()

        songs.forEachIndexed { index, song ->
            val title = song.getTitle()
            val firstChar = BackEnd.removePrefix(title).firstOrNull()?.uppercase()

            if (firstChar != null && !letterToFirstInstance.containsKey(firstChar)) {
                letterToFirstInstance[firstChar] = index
            }
        }

        return letterToFirstInstance
    }

//    private fun createSongButton(audio: Song, holder: SongButtonViewHolder): View {
//        val songButton = UI.createButton(context, audio)
//
//        songButton.setOnClickListener {
//            holder.floatingTitle.text = audio.getTitle()
//            holder.floatingArtist.text = audio.getArtist()
//            holder.floatingButtons.visibility = View.VISIBLE
//
//            if (MusicPlayer.checkIfPlayerEmpty() != null) {
//                MusicPlayer.stop()
//                MusicPlayer.release()
//            }
//
//            MusicPlayer.createPlayer(context, audio.getUri())
//            MusicPlayer.play()
//
//            SongOrder.createDefaultOrder(generatedSongOrder)
//            SongOrder.setCurrentOrder(SongOrder.getDefault().toMutableMap())
//            SongOrder.setCurrentSong(audio)
//        }
//
//        return songButton
//    }
}