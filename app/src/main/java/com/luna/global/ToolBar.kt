package com.luna.global

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import com.luna.main.R
import com.luna.main.StartUp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import com.luna.utils.UI

//TODO: might combine everything into a single function/class/something
object ToolBar {
    fun refresh(activity:Activity) {
        val refreshButton: ImageButton = activity.findViewById(R.id.refreshButton)

        // Define color for different states
//        val stateListDrawable = StateListDrawable()
//
//        // Define color for different states
//        val currentColor = ContextCompat.getColor(activity, R.color.white) //android.R.color.transparent
//        val colorPressed = ContextCompat.getColor(activity, R.color.light_gray)
//        val colorFocused = ContextCompat.getColor(activity, R.color.light_gray)
//
//        val circleDefault = UI.createCircleDrawable(refreshButton, currentColor, 12, 12)
//        val circlePressed = UI.createCircleDrawable(refreshButton, colorPressed, 12, 12)
//        val circleFocused = UI.createCircleDrawable(refreshButton, colorFocused, 12, 12)
//
//        stateListDrawable.addState(intArrayOf(android.R.attr.state_pressed), circlePressed)
//        stateListDrawable.addState(intArrayOf(android.R.attr.state_focused), circleFocused)
//        stateListDrawable.addState(intArrayOf(), circleDefault)
//
//        refreshButton.background = stateListDrawable

//        refreshButton.setBackgroundResource(R.drawable.ic_circle_selector)

        refreshButton.setOnClickListener {
//            refreshButton.setBackgroundColor(ContextCompat.getColor(activity, R.color.light_gray))
//            CoroutineScope(Dispatchers.IO).launch {
//                (activity.application as StartUp).getAllAudioFiles(activity)
//
//                withContext(Dispatchers.Main) {
//                    activity.recreate()
//                }
//            }
        }
    }

    fun search(activity: Activity) {

    }
}