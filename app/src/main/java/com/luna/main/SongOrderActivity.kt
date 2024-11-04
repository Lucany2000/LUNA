package com.luna.main

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.StateListDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.luna.data.Song
import com.luna.global.SongOrder
import com.luna.utils.UI

class SongOrderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.song_order)

        main()
    }

    private fun main() {
        val songOrder = findViewById<LinearLayout>(R.id.songOrder)

        SongOrder.getCurrentOrder().map { (index, song) ->
            val button = tempCreateSongButton(song)
            val separator = UI.createSeparator(this)

            songOrder.addView(button)
            songOrder.addView(separator)

            Pair(button, separator)
        }
    }

    fun tempCreateSongButton(audio: Song): LinearLayout {
        val compoundTextView = LinearLayout(this)
        compoundTextView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        compoundTextView.orientation = LinearLayout.VERTICAL
        compoundTextView.gravity = Gravity.CENTER

        val titleTextView = UI.createTextView(this, audio.getTitle(), true, compoundTextView)
        val artistTextView = UI.createTextView(this, audio.getArtist(), false, compoundTextView)

        compoundTextView.addView(titleTextView)
        compoundTextView.addView(artistTextView)

        val currentColor = ContextCompat.getColor(this, R.color.white)

        val stateListDrawable = StateListDrawable()
        stateListDrawable.addState(intArrayOf(), ColorDrawable(currentColor))

        compoundTextView.background = stateListDrawable

        return compoundTextView
    }

}