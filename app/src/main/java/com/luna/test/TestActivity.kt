package com.luna.test

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.luna.main.R

class TestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.playground)
        main()
    }

    private fun main() {
//        generateCharLine()

    }

    fun generateCharLine() {
        val charLine = findViewById<LinearLayout>(R.id.charLine)

        val alphabet = ('A'..'Z').toMutableList()

        alphabet.addAll('1'..'9')

        alphabet.map {
            val letter = TextView(this@TestActivity)
            letter.text = it.toString()
            letter.height = 100
            letter.width = 100

            letter.gravity = Gravity.CENTER
            letter.textSize = 16f


            val currentColor = ContextCompat.getColor(this, R.color.light_gray)

//            val colorPressed = Color.BLUE

            letter.background = ColorDrawable(currentColor)

            charLine.addView(letter)
        }
    }
}