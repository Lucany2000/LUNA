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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InitializationActivity: AppCompatActivity() {

    private val REQUEST_PERMISSION_CODE = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if ((application as StartUp).hasStoragePermission(this)) {
            startActivity(Intent(this@InitializationActivity, MainActivity::class.java))
            finish()
        } else {
            requestStoragePermissions()
        }
    }

    private fun requestStoragePermissions() {
        val permissions = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
            arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            CoroutineScope(Dispatchers.Main).launch {
                (application as StartUp).getAllAudioFiles(this@InitializationActivity) // Fetch audio files
                startActivity(Intent(this@InitializationActivity, MainActivity::class.java))
                finish()
            }
        } else {
            errorMsg("No Audio Found", this)
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