package com.luna.utils

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

import com.luna.main.R

class VisibilityControl : AppCompatActivity() {

    private lateinit var textView: TextView
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.floating_button)

        textView = findViewById(R.id.iconContainer)
//        imageView = findViewById(R.id.imageView)

        // Condition to check (for demonstration)
        val condition = true // Change this to false to test the other case

        if (condition) {
            // Show TextView if condition is true
            textView.visibility = View.VISIBLE
            imageView.visibility = View.GONE // Hide ImageView
        } else {
            // Show ImageView if condition is false
            imageView.visibility = View.VISIBLE
            textView.visibility = View.GONE // Hide TextView
        }
    }
}
