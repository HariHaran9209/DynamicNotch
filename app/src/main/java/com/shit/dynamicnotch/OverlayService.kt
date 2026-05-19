package com.yourname.dynamicnotch

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.session.MediaSessionManager
import android.os.IBinder
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.NotificationCompat
import java.text.SimpleDateFormat
import java.util.*

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private lateinit var notifTitle: TextView
    private lateinit var timeText: TextView

    companion object {
        const val ACTION_SHOW_NOTIFICATION = "com.yourname.dynamicnotch.SHOW_NOTIF"
        const val EXTRA_TITLE = "title"
        const val EXTRA_TEXT = "text"
        const val EXTRA_APP = "app"
    }

    private val notifReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val title = intent.getStringExtra(EXTRA_TITLE) ?: return
            val text = intent.getStringExtra(EXTRA_TEXT) ?: ""
            showInBar("$title — $text")
        }
    }

    private val chargingReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_POWER_CONNECTED -> showInBar("⚡ Charging started")
                Intent.ACTION_POWER_DISCONNECTED -> showInBar("🔋 Unplugged")
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        startForegroundNotification()
        setupOverlayView()
        registerReceivers()
    }

    private fun startForegroundNotification() {
        val channelId = "dynamic_notch"
        val channel = NotificationChannel(
            channelId, "Dynamic Notch",
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Dynamic Notch Running")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()

        startForeground(1, notification)
    }

    private fun setupOverlayView() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_bar, null)
        notifTitle = overlayView.findViewById(R.id.notifTitle)
        timeText = overlayView.findViewById(R.id.timeText)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP
        }

        windowManager.addView(overlayView, params)
        updateClock()
    }

    private fun updateClock() {
        val timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                overlayView.post { timeText.text = time }
            }
        }, 0, 30000)
    }

    private fun registerReceivers() {
        registerReceiver(notifReceiver, IntentFilter(ACTION_SHOW_NOTIFICATION),
            RECEIVER_NOT_EXPORTED)
        val chargingFilter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        registerReceiver(chargingReceiver, chargingFilter, RECEIVER_NOT_EXPORTED)
    }

    fun showInBar(message: String) {
        overlayView.post {
            notifTitle.text = message
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        windowManager.removeView(overlayView)
        unregisterReceiver(notifReceiver)
        unregisterReceiver(chargingReceiver)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}