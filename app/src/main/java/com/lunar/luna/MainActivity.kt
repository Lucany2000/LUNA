package com.lunar.luna

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnClickMe = findViewById<Button>(R.id.mybutton)
        val changeText = findViewById<TextView>(R.id.textView)
        btnClickMe.text = "Click me"
        changeText.text = "0"
        var timesClicked = 0
        btnClickMe.setOnClickListener{
            //btnClickMe.text = "Clicked Me"
            timesClicked += 1
            changeText.text = timesClicked.toString() //"Hello User"
            Toast.makeText(this, "Hey, I'm toast", Toast.LENGTH_LONG).show()
        }
    }
}