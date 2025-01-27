package com.luna.utils

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.text.TextUtils
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.luna.data.Song
import com.luna.global.MusicPlayer
import com.luna.global.SongOrder
import com.luna.main.R
import com.luna.main.SongOrderActivity

object UI {

    //Buttons

    fun createButton(context: Context, song: Song): LinearLayout {
        val compoundTextView = LinearLayout(context)
        compoundTextView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        compoundTextView.orientation = LinearLayout.VERTICAL
        compoundTextView.gravity = Gravity.CENTER

        val titleTextView = createTextView(context, song.getTitle(), true, compoundTextView)
        val artistTextView = createTextView(context, song.getArtist(), false, compoundTextView)

        val currentColor = ContextCompat.getColor(context, R.color.white)

        val colorPressed = ContextCompat.getColor(context, R.color.light_gray)

        compoundTextView.addView(titleTextView)
        compoundTextView.addView(artistTextView)

        val stateListDrawable = StateListDrawable()
        stateListDrawable.addState(intArrayOf(android.R.attr.state_pressed), ColorDrawable(colorPressed))
        stateListDrawable.addState(intArrayOf(android.R.attr.state_focused), ColorDrawable(colorPressed))
        stateListDrawable.addState(intArrayOf(android.R.attr.state_activated), ColorDrawable(colorPressed))
        stateListDrawable.addState(intArrayOf(), ColorDrawable(currentColor))

        compoundTextView.background = stateListDrawable


        return compoundTextView
    }

    fun createTextView(context: Context, text: String, isTitle: Boolean, parent: LinearLayout): TextView {
        val textView = TextView(context)
        textView.text = text
        textView.maxLines = 1
        textView.ellipsize = TextUtils.TruncateAt.END

        // Customize font size and rotation based on whether it's a title or artist
        textView.textSize = if (isTitle) 20f else 18f

        //TODO: text slider if (text.length > parent.width)
//        textView.rotation = if (text.length > parent.width) 90f else 0f

        return textView
    }

    fun createSeparator(context: Context): View {
        val separator = View(context)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            context.resources.getDimensionPixelSize(R.dimen.separator_height)
        )

        separator.layoutParams = layoutParams
        separator.setBackgroundColor(ContextCompat.getColor(context, R.color.black))

        return separator
    }

    //Text Bubble

    fun createCharButton(context: Context, char: Char): TextView {
        val button = TextView(context)
        button.text = char.toString()
        button.width = 100
        button.height = 100

        button.gravity = Gravity.CENTER
        button.textSize = 16f


        val currentColor = ContextCompat.getColor(context, R.color.light_gray)

        button.background = ColorDrawable(currentColor)


        return button
    }



    fun showBubbleText(context: Context, anchorView: View, bubbleText: CharSequence): PopupWindow {

        val ovalShape = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.BLUE)
            setSize(150, 150) // Set your desired size
        }

        // Create a LinearLayout to hold the bubble text
        val bubbleLayout = LinearLayout(context)
        bubbleLayout.orientation = LinearLayout.VERTICAL
        bubbleLayout.background = ovalShape
        bubbleLayout.gravity = Gravity.CENTER
//        bubbleLayout.setBackgroundResource(R.drawable.ic_circle) // Customize bubble background


//        Toast.makeText(this,"bubble", Toast.LENGTH_SHORT).show()

        // Create a TextView for the bubble text
        val bubbleTextView = TextView(context)
        bubbleTextView.text = bubbleText
        bubbleTextView.textSize = 16f
        bubbleTextView.gravity = Gravity.CENTER
        bubbleTextView.setTextColor(ContextCompat.getColor(context, R.color.white)) // Customize text color
        bubbleTextView.setPadding(16, 8, 16, 8)

        // Add the TextView to the LinearLayout
        bubbleLayout.addView(bubbleTextView)

        // Create a PopupWindow with the bubble text layout
        val popupWindow = PopupWindow(
            bubbleLayout,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        // Show the PopupWindow below the anchor view
        popupWindow.showAsDropDown(anchorView, -275, -anchorView.height, Gravity.TOP)

        bubbleLayout.setOnClickListener {
            popupWindow.dismiss()
        }

        return popupWindow

    }

//    fun showBubbleText(context: Context, anchorView: View, bubbleText: CharSequence, dismiss: PopupWindow?) {
//
//        val ovalShape = GradientDrawable().apply {
//            shape = GradientDrawable.OVAL
//            setColor(Color.BLUE)
//            setSize(150, 150) // Set your desired size
//        }
//
//        // Create a LinearLayout to hold the bubble text
//        val bubbleLayout = LinearLayout(context)
//        bubbleLayout.orientation = LinearLayout.VERTICAL
//        bubbleLayout.background = ovalShape
//        bubbleLayout.gravity = Gravity.CENTER
////        bubbleLayout.setBackgroundResource(R.drawable.ic_circle) // Customize bubble background
//
//
////        Toast.makeText(this,"bubble", Toast.LENGTH_SHORT).show()
//
//        // Create a TextView for the bubble text
//        val bubbleTextView = TextView(context)
//        bubbleTextView.text = bubbleText
//        bubbleTextView.textSize = 16f
//        bubbleTextView.gravity = Gravity.CENTER
//        bubbleTextView.setTextColor(ContextCompat.getColor(this, R.color.white)) // Customize text color
//        bubbleTextView.setPadding(16, 8, 16, 8)
//
//        // Add the TextView to the LinearLayout
//        bubbleLayout.addView(bubbleTextView)
//
//        // Create a PopupWindow with the bubble text layout
//        val popupWindow = PopupWindow(
//            bubbleLayout,
//            LinearLayout.LayoutParams.WRAP_CONTENT,
//            LinearLayout.LayoutParams.WRAP_CONTENT
//        )
//
//        // Show the PopupWindow below the anchor view
//        popupWindow.showAsDropDown(anchorView, -275, -anchorView.height, Gravity.TOP)
//
//        dismiss = popupWindow
//
//        bubbleLayout.setOnClickListener {
//            popupWindow.dismiss()
//        }
//
//    }
}