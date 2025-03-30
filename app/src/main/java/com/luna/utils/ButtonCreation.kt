package com.luna.utils

import CharButtonAdapter
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.StateListDrawable
import android.util.Log
import android.view.Gravity
import android.view.MenuInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.luna.data.Song
import com.luna.global.MusicPlayer
import com.luna.global.SongOrder
import com.luna.main.R
import com.luna.main.SongOrderActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

open class ButtonCreation(protected val activity: AppCompatActivity) {
    private val query: QueryTable = QueryTable(activity)
    private lateinit var songAdapter: RecycleViewAdapter<Song>
    private lateinit var charAdapter: CharButtonAdapter
    private lateinit var generatedSongList: List<Song>
    private val letterToFirstInstance: MutableMap<String, Int> = mutableMapOf()
    private lateinit var uniqueChars: MutableList<Char>

    /**
     *
     * @param item usage is based on type. If of type: String, then it is implied to be an artist button.
     * Type: Song implies a song button and type: Pair implies an album button.
     *
     * @param type applies to albums only with options: window and none. Window simply adds an enlarged image to the button.
     *
     */
    private inline fun <reified T> createButton(context: Context, item: T, type: String? = null): LinearLayout {
        val compoundTextView = LinearLayout(context)
        compoundTextView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        compoundTextView.orientation = LinearLayout.VERTICAL
        compoundTextView.gravity = Gravity.CENTER

        when (T::class){
            Song::class -> {
                item as Song
                val titleTextView =
                    UI.createTextView(context, item.getTitle(), true, compoundTextView)
                val artistTextView =
                    UI.createTextView(context, item.getArtist(), false, compoundTextView)

                compoundTextView.addView(titleTextView)
                compoundTextView.addView(artistTextView)
            }

            String::class -> {
                item as String
                val titleTextView = UI.createTextView(context, item, true, compoundTextView)

                compoundTextView.addView(titleTextView)
            }

            Pair::class -> {
                when (type) {
                    "none" -> {
                        @Suppress("UNCHECKED_CAST")
                        item as Pair<String, String>
                        val titleTextView =
                            UI.createTextView(context, item.first, true, compoundTextView)
                        val artistTextView =
                            UI.createTextView(context, item.second, false, compoundTextView)

                        compoundTextView.addView(titleTextView)
                        compoundTextView.addView(artistTextView)
                    }
                    "window" -> {
                        //TODO: Add image later

                        @Suppress("UNCHECKED_CAST")
                        item as Pair<String, String>
                        val titleTextView =
                            UI.createTextView(context, item.first, true, compoundTextView)
                        val artistTextView =
                            UI.createTextView(context, item.second, false, compoundTextView)

                        compoundTextView.addView(titleTextView)
                        compoundTextView.addView(artistTextView)
                    }
                }
            }
        }



        val currentColor = ContextCompat.getColor(context, R.color.white)
        val colorPressed = ContextCompat.getColor(context, R.color.light_gray)
        val stateListDrawable = StateListDrawable()
        stateListDrawable.addState(intArrayOf(android.R.attr.state_pressed), ColorDrawable(colorPressed))
        stateListDrawable.addState(intArrayOf(android.R.attr.state_focused), ColorDrawable(colorPressed))
        stateListDrawable.addState(intArrayOf(android.R.attr.state_activated), ColorDrawable(colorPressed))
        stateListDrawable.addState(intArrayOf(), ColorDrawable(currentColor))

        compoundTextView.background = stateListDrawable


        return compoundTextView
    }


    open fun createSongButton(activity: AppCompatActivity, audio: Song): LinearLayout  {
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
            if (!it.isEnabled) return@setOnClickListener
            UI.setClickCooldown(it)
            val intent = Intent(activity, SongOrderActivity::class.java)
            activity.startActivity(intent)
        }


        return songButtonContainer
    }

}