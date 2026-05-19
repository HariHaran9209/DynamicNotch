package com.yourname.dynamicnotch

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val statusText = findViewById<TextView>(R.id.statusText)
        val btnOverlay = findViewById<Button>(R.id.btnOverlay)
        val btnNotif = findViewById<Button>(R.id.btnNotif)
        val btnStart = findViewById<Button>(R.id.btnStart)

        btnOverlay.setOnClickListener {
            // Opens MIUI overlay permission screen
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }

        btnNotif.setOnClickListener {
            // Opens notification listener settings
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        btnStart.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                statusText.text = "⚠️ Please grant Overlay permission first"
                return@setOnClickListener
            }
            startForegroundService(Intent(this, OverlayService::class.java))
            statusText.text = "✅ Notch bar is running!"
        }
    }

    override fun onResume() {
        super.onResume()
        val statusText = findViewById<TextView>(R.id.statusText)
        if (Settings.canDrawOverlays(this)) {
            statusText.text = "Overlay permission granted ✅"
        }
    }
}