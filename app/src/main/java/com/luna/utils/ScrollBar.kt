package com.luna.utils

import com.luna.main.R
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import android.util.Log
import org.w3c.dom.Text
//
//class ScrollBar(context: Context, attrs: AttributeSet?) : View(context, attrs) {
//
//    private var rectPaint: Paint = Paint().apply {
//        color = Color.BLUE
//        style = Paint.Style.FILL
//        alpha = 128 // Set transparency (0-255 range)
//    }
//
//    private var rect: Rect? = null
//    private var lastY = 0f // To track the last Y position for vertical dragging
//
//    private var parentLayout: LinearLayout? = null
//    private var scrollTrack: ScrollView? = null
//    private val childrenMap: MutableMap<Int, List<Int>> = mutableMapOf()
//
//    override fun onDraw(canvas: Canvas) {
//        super.onDraw(canvas)
//        rect?.let {
//            canvas.drawRect(it, rectPaint)
//        }
//    }
//
//    override fun onTouchEvent(event: MotionEvent): Boolean {
//        when (event.action) {
//            MotionEvent.ACTION_DOWN -> {
//                if (isWithinParent(event.x, event.y)) {
//
//                    val top = event.y.toInt() - 150
//                    val bottom = event.y.toInt() + 150
//
//                    rect = Rect(parentLayout!!.left, top, parentLayout!!.right, bottom)
//                    lastY = event.y
//                    invalidate()
//                }
//
//                parentLayout?.post {
//                    val parentTop = parentLayout!!.top
//                    val parentBottom = parentLayout!!.bottom
//                    val childrenArray = getChilden(top, bottom)
//
//                    val topFiftyPer = hatchMark(parentTop, parentLayout!!.getChildAt(0).bottom)
//                    val bottomFiftyPer = hatchMark(parentBottom, parentLayout!!.getChildAt(parentLayout!!.childCount - 1).top)
//
//                    if (top < parentTop + topFiftyPer) {
//                        val firstChild = childrenArray[0]
//                        firstChild.performClick()
//                    }
//                    else if (bottom > parentBottom - bottomFiftyPer) {
//                        val midChild = childrenArray[2]
//                        midChild.performClick()
//
//                    } else {
//                        val lastChild = childrenArray[1]
//                        lastChild.performClick()
//                    }
//                }
//
//            }
//            MotionEvent.ACTION_MOVE -> {
//                rect?.let {
//
//                    val deltaY = (event.y - lastY).toInt()
//                    val newTop = it.top + deltaY
//                    val newBottom = it.bottom + deltaY
//
//                    // Ensure the new position is within the parent's bounds
//                    if (newTop >= 0 && newBottom <= parentLayout?.height ?: 0) {
//                        it.offset(0, deltaY)
//                        lastY = event.y
//                        invalidate()
//                    }
//
//                    parentLayout?.post {
//                        val parentTop = parentLayout!!.top
//                        val parentBottom = parentLayout!!.bottom
//                        val childrenArray = getChilden(top, bottom)
//
//                        val topFiftyPer = hatchMark(parentTop, parentLayout!!.getChildAt(0).bottom)
//                        val bottomFiftyPer = hatchMark(parentBottom, parentLayout!!.getChildAt(parentLayout!!.childCount - 1).top)
//
//                        if (top < parentTop + topFiftyPer) {
//                            val firstChild = childrenArray[0]
//                            firstChild.performClick()
//                        }
//                        else if (bottom > parentBottom - bottomFiftyPer) {
//                            val midChild = childrenArray[2]
//                            midChild.performClick()
//
//                        } else {
//                            val lastChild = childrenArray[1]
//                            lastChild.performClick()
//                        }
//                    }
//                }
//            }
//            MotionEvent.ACTION_UP -> {
//                rect = null
//                invalidate()
//                // Clear the rectangle on touch up
//            }
//        }
//        return true
//    }
//
//    private fun isWithinParent(x: Float, y: Float): Boolean {
//        parentLayout?.let {
//            val location = intArrayOf(0, 0)
//            it.getLocationOnScreen(location)
//            val parentX = location[0]
//            val parentY = location[1]
//
//            return x >= parentX && x <= (parentX + it.width) && y >= parentY && y <= (parentY + it.height)
//        }
//        return false
//    }
//
//    private fun getChilden(top: Int, bottom: Int): MutableList<TextView> {
//        val childBasket = childrenMap.filterValues {
//            val fiftyPercent = hatchMark(it[0], it[1])
//            (top >= it[0] && top <= (it[1]-fiftyPercent)) ||
//                    (bottom >= (it[0]+fiftyPercent) && bottom <= it[1]) ||
//                    (it[0] > top && it[1] < bottom)
//        }
//
//        //TODO: Run tests on likelyhood of a List > length 3
//        val children = mutableListOf<TextView>()
//
//        childBasket.keys.forEach{
//            val child = parentLayout?.getChildAt(it) as TextView
//            children.add(child)
//        }
//
//        return children
//    }
//
//    private fun hatchMark(top: Int, bottom: Int): Int {
//        return (bottom - top).floorDiv(2)
//    }
//
//    override fun onAttachedToWindow() {
//        super.onAttachedToWindow()
//        parentLayout = (parent as? FrameLayout)?.findViewById(R.id.charLine)
//        scrollTrack = (parent as? FrameLayout)?.findViewById(R.id.charLineScrollView)
//        parentLayout?.let { parent ->
//            for (i in 0 until parent.childCount) {
//                val child = parent.getChildAt(i) as TextView
//                child.post{
//                    val top = child.top
//                    val bottom = child.bottom
//
//                    childrenMap.put(i, listOf(top, bottom))
//                }
//            }
//        }
//    }
//}

//TODO: combine scrollBar, bubble text, and fast travel

//fun showBubbleText(anchorView: View, bubbleText: CharSequence) {
//
//    val ovalShape = GradientDrawable().apply {
//        shape = GradientDrawable.OVAL
//        setColor(Color.BLUE)
//        setSize(150, 150) // Set your desired size
//    }
//
//    // Create a LinearLayout to hold the bubble text
//    val bubbleLayout = LinearLayout(this)
//    bubbleLayout.orientation = LinearLayout.VERTICAL
//    bubbleLayout.background = ovalShape
//    bubbleLayout.gravity = Gravity.CENTER
////        bubbleLayout.setBackgroundResource(R.drawable.ic_circle) // Customize bubble background
//
//
////        Toast.makeText(this,"bubble", Toast.LENGTH_SHORT).show()
//
//    // Create a TextView for the bubble text
//    val bubbleTextView = TextView(this)
//    bubbleTextView.text = bubbleText
//    bubbleTextView.textSize = 16f
//    bubbleTextView.gravity = Gravity.CENTER
//    bubbleTextView.setTextColor(ContextCompat.getColor(this, R.color.white)) // Customize text color
//    bubbleTextView.setPadding(16, 8, 16, 8)
//
//    // Add the TextView to the LinearLayout
//    bubbleLayout.addView(bubbleTextView)
//
//    // Create a PopupWindow with the bubble text layout
//    val popupWindow = PopupWindow(
//        bubbleLayout,
//        LinearLayout.LayoutParams.WRAP_CONTENT,
//        LinearLayout.LayoutParams.WRAP_CONTENT
//    )
//
//    // Show the PopupWindow below the anchor view
//    popupWindow.showAsDropDown(anchorView, -275, -anchorView.height, Gravity.TOP)
//
//    dismissPopupWindow = popupWindow
//
//    bubbleLayout.setOnClickListener {
//        popupWindow.dismiss()
//    }
//
//}