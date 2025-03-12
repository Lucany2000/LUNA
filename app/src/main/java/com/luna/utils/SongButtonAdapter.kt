package com.luna.utils

import android.content.Context
import android.util.SparseArray
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

    //TODO: Fixed Crashes for now. Testing required.

    private val viewCache = SparseArray<Pair<LinearLayout,View>>()

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
//        val songButton = createSongButton(song)
//        holder.songButtonContainer.removeAllViews()
//        holder.songButtonContainer.addView(songButton)
//        val separator = UI.createSeparator(context)
//        holder.songButtonContainer.addView(separator)

//        var cachedPair: Pair<LinearLayout, View> = viewCache.get(position)

        val cachedPair = viewCache[position] ?: run {
            val songButton = createSongButton(song)
            val separator = UI.createSeparator(context)
            Pair(songButton, separator).also { viewCache[position] = it }
        }

        val (songButton, separator) = cachedPair

        (songButton.parent as? ViewGroup)?.removeView(songButton)
        (separator.parent as? ViewGroup)?.removeView(separator)

        holder.songButtonContainer.removeAllViews() // Clear previous view if exists
        holder.songButtonContainer.addView(songButton)
        holder.songButtonContainer.addView(separator)

        //TODO: Find a way to get the XML separator to work
//        val separator = holder.itemView.findViewById<View>(R.id.separator)
//        if (separator != null) {
//            holder.songButtonContainer.addView(separator)
//        }
    }

    override fun getItemCount() = songs.size

}