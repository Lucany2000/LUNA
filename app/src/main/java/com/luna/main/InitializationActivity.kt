package com.luna.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InitializationActivity: AppCompatActivity() {

    private val REQUEST_PERMISSION_CODE = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_AUDIO
            ) != PackageManager.PERMISSION_GRANTED ||

            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted, request it
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_MEDIA_AUDIO),
                    REQUEST_PERMISSION_CODE
                )
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_PERMISSION_CODE
                )
            }
        } else {

            CoroutineScope(Dispatchers.Main).launch {
                val init = application as StartUp

                init.audioFiles = init.getAllAudioFiles(this@InitializationActivity)

                val intent = Intent(this@InitializationActivity, MainActivity::class.java)

                startActivity(intent)

            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_PERMISSION_CODE) {
            // Check if permissions are granted
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {

                CoroutineScope(Dispatchers.Main).launch {
                    val init = application as StartUp
                    init.audioFiles = init.getAllAudioFiles(this@InitializationActivity)

                    val intent = Intent(this@InitializationActivity, MainActivity::class.java)

                    startActivity(intent)
                    finish()
                }


            } else {
                errorMsg("No Audio Found", this)
            }
        }
    }

    fun errorMsg(text: String, context: Context) {

//        val rootLayout = findViewById<LinearLayout>(R.id.rootLayout)
        val rootLayout = LinearLayout(context)
        rootLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        rootLayout.orientation = LinearLayout.VERTICAL
        rootLayout.gravity = Gravity.CENTER
        // Set a solid color background (you can use Color.parseColor for hex colors)

        val textView = TextView(context)
        textView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        textView.text = text
        textView.gravity = Gravity.CENTER
        textView.setTextColor(Color.BLACK)

        textView.append("\n\nTo enable the permission, go to app settings.")
        textView.setOnClickListener {
            openAppSettings()
        }

        textView.setTextSize(24f)

        rootLayout.addView(textView)
        setContentView(rootLayout)
    }

    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }
}