package com.luna.utils

import com.luna.main.R
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout

class ScrollBar(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var rectPaint: Paint = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.FILL
        alpha = 128 // Set transparency (0-255 range)
    }

    private var rect: Rect? = null
    private var lastY = 0f // To track the last Y position for vertical dragging
    private var parentLinearLayout: LinearLayout? = null

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        rect?.let {
            canvas.drawRect(it, rectPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (isWithinParent(event.x, event.y)) {

                    rect = Rect(parentLinearLayout!!.left, event.y.toInt()-100, parentLinearLayout!!.right, event.y.toInt() + 100)
                    lastY = event.y
                    invalidate()
                }
            }
            MotionEvent.ACTION_MOVE -> {
                rect?.let {

                    val deltaY = (event.y - lastY).toInt()
                    val newTop = it.top + deltaY
                    val newBottom = it.bottom + deltaY

                    // Ensure the new position is within the parent's bounds
                    if (newTop >= 0 && newBottom <= parentLinearLayout?.height ?: 0) {
                        it.offset(0, deltaY)
                        lastY = event.y
                        invalidate()
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                // Clear the rectangle on touch up
                rect = null
                invalidate()
            }
        }
        return true
    }

    private fun isWithinParent(x: Float, y: Float): Boolean {
        parentLinearLayout?.let {
            val location = intArrayOf(0, 0)
            it.getLocationOnScreen(location)
            val parentX = location[0]
            val parentY = location[1]

            return x >= parentX && x <= (parentX + it.width) && y >= parentY && y <= (parentY + it.height)
        }
        return false
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        parentLinearLayout = (parent as? FrameLayout)?.findViewById(R.id.charLineTest)
    }
}