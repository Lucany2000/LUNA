package com.luna.utils

import android.content.Context
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView

import com.luna.data.Song
import com.luna.main.R

class RecycleViewAdapter<T>(
    private val context: Context,
    private var list: List<T>,
    private val createButton: (T) -> LinearLayout): RecyclerView.Adapter<RecycleViewAdapter.ButtonViewHolder>() {

    //TODO: Fixed Crashes for now. Testing required.

    private val viewCache = SparseArray<Pair<LinearLayout,View>>()

    class ButtonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ButtonContainer: LinearLayout = view.findViewById(R.id.mainButtonContainer)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ButtonViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.main_button, parent, false)
        return ButtonViewHolder(view)
    }

    override fun onBindViewHolder(holder: ButtonViewHolder, position: Int) {
        val item = list[position]

        // Create song button dynamically
//        val songButton = createSongButton(song)
//        holder.songButtonContainer.removeAllViews()
//        holder.songButtonContainer.addView(songButton)
//        val separator = UI.createSeparator(context)
//        holder.songButtonContainer.addView(separator)

//        var cachedPair: Pair<LinearLayout, View> = viewCache.get(position)

        val cachedPair = viewCache[position] ?: run {
            val songButton = createButton(item)
            val separator = UI.createSeparator(context)
            Pair(songButton, separator).also { viewCache[position] = it }
        }

        val (songButton, separator) = cachedPair

        (songButton.parent as? ViewGroup)?.removeView(songButton)
        (separator.parent as? ViewGroup)?.removeView(separator)

        holder.ButtonContainer.removeAllViews() // Clear previous view if exists
        holder.ButtonContainer.addView(songButton)
        holder.ButtonContainer.addView(separator)

        //TODO: Find a way to get the XML separator to work
//        val separator = holder.itemView.findViewById<View>(R.id.separator)
//        if (separator != null) {
//            holder.songButtonContainer.addView(separator)
//        }
    }

    override fun getItemCount() = list.size

    fun updateAdapter(newList: List<T>) {
        list.toMutableList().clear()
        list = newList.toList()
        notifyDataSetChanged()
    }

}