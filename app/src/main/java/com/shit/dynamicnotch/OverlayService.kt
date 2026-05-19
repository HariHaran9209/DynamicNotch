package com.yourname.dynamicnotch

import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.*
import android.view.*
import android.view.animation.OvershootInterpolator
import android.widget.*
import androidx.core.app.NotificationCompat

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View

    private lateinit var idleView: View
    private lateinit var notifView: View
    private lateinit var musicView: View
    private lateinit var callView: View
    private lateinit var chargingView: View

    private lateinit var notifIcon: ImageView
    private lateinit var notifApp: TextView
    private lateinit var notifTitle: TextView
    private lateinit var notifText: TextView
    private lateinit var replyBox: LinearLayout
    private lateinit var replyInput: TextView
    private lateinit var sendBtn: TextView
    private lateinit var btnReply: TextView
    private lateinit var btnRead: TextView

    private lateinit var albumArt: ImageView
    private lateinit var musicTitle: TextView
    private lateinit var musicArtist: TextView
    private lateinit var btnPlayPause: TextView
    private lateinit var btnPrev: TextView
    private lateinit var btnNext: TextView

    private lateinit var callerName: TextView
    private lateinit var btnAccept: TextView
    private lateinit var btnDecline: TextView

    private lateinit var chargingLevel: TextView

    private var activeMediaController: MediaController? = null
    private var autoHideRunnable: Runnable? = null
    private val handler = Handler(Looper.getMainLooper())

    companion object {
        const val ACTION_SHOW_NOTIFICATION = "com.yourname.dynamicnotch.SHOW_NOTIF"
        const val ACTION_SHOW_CALL = "com.yourname.dynamicnotch.SHOW_CALL"
        const val EXTRA_TITLE = "title"
        const val EXTRA_TEXT = "text"
        const val EXTRA_APP = "app"
        const val EXTRA_PACKAGE = "package"
        const val EXTRA_NOTIF_KEY = "notif_key"
        const val EXTRA_CALLER = "caller"
    }

    private val notifReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_SHOW_NOTIFICATION -> {
                    val title = intent.getStringExtra(EXTRA_TITLE) ?: return
                    val text = intent.getStringExtra(EXTRA_TEXT) ?: ""
                    val pkg = intent.getStringExtra(EXTRA_PACKAGE) ?: ""
                    val key = intent.getStringExtra(EXTRA_NOTIF_KEY) ?: ""
                    showNotification(title, text, pkg, key)
                }
                ACTION_SHOW_CALL -> {
                    val caller = intent.getStringExtra(EXTRA_CALLER) ?: "Unknown"
                    showCall(caller)
                }
            }
        }
    }

    private val chargingReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_POWER_CONNECTED -> {
                    val bm = getSystemService(BATTERY_SERVICE) as BatteryManager
                    val level = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                    showCharging(level)
                }
                Intent.ACTION_POWER_DISCONNECTED -> collapseToIdle()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        startForegroundNotification()
        setupOverlay()
        registerReceivers()
        startMusicWatcher()
    }

    private fun startForegroundNotification() {
        val channelId = "dynamic_notch"
        val channel = NotificationChannel(
            channelId, "Dynamic Notch", NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Dynamic Notch Active")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
        startForeground(1, notification)
    }

    private fun setupOverlay() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_island, null)

        idleView = overlayView.findViewById(R.id.idleView)
        notifView = overlayView.findViewById(R.id.notifView)
        musicView = overlayView.findViewById(R.id.musicView)
        callView = overlayView.findViewById(R.id.callView)
        chargingView = overlayView.findViewById(R.id.chargingView)

        notifIcon = overlayView.findViewById(R.id.notifIcon)
        notifApp = overlayView.findViewById(R.id.notifApp)
        notifTitle = overlayView.findViewById(R.id.notifTitle)
        notifText = overlayView.findViewById(R.id.notifText)
        replyBox = overlayView.findViewById(R.id.replyBox)
        replyInput = overlayView.findViewById(R.id.replyInput)
        sendBtn = overlayView.findViewById(R.id.sendBtn)
        btnReply = overlayView.findViewById(R.id.btnReply)
        btnRead = overlayView.findViewById(R.id.btnRead)

        albumArt = overlayView.findViewById(R.id.albumArt)
        musicTitle = overlayView.findViewById(R.id.musicTitle)
        musicArtist = overlayView.findViewById(R.id.musicArtist)
        btnPlayPause = overlayView.findViewById(R.id.btnPlayPause)
        btnPrev = overlayView.findViewById(R.id.btnPrev)
        btnNext = overlayView.findViewById(R.id.btnNext)

        callerName = overlayView.findViewById(R.id.callerName)
        btnAccept = overlayView.findViewById(R.id.btnAccept)
        btnDecline = overlayView.findViewById(R.id.btnDecline)

        chargingLevel = overlayView.findViewById(R.id.chargingLevel)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = 10
        }

        windowManager.addView(overlayView, params)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        btnReply.setOnClickListener {
            replyBox.visibility = View.VISIBLE
            val params = overlayView.layoutParams as WindowManager.LayoutParams
            params.flags = params.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
            windowManager.updateViewLayout(overlayView, params)
            replyInput.requestFocus()
        }

        sendBtn.setOnClickListener {
            val replyText = replyInput.text.toString()
            if (replyText.isNotBlank()) {
                sendReply(replyText)
                replyInput.text = ""
                replyBox.visibility = View.GONE
                val params = overlayView.layoutParams as WindowManager.LayoutParams
                params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                windowManager.updateViewLayout(overlayView, params)
                collapseToIdle()
            }
        }

        btnRead.setOnClickListener {
            NotificationListener.currentNotif?.let {
                NotificationListener.instance?.cancelNotification(it.key)
            }
            collapseToIdle()
        }

        btnPlayPause.setOnClickListener {
            val state = activeMediaController?.playbackState?.state
            if (state == PlaybackState.STATE_PLAYING) {
                activeMediaController?.transportControls?.pause()
                btnPlayPause.text = "▶"
            } else {
                activeMediaController?.transportControls?.play()
                btnPlayPause.text = "⏸"
            }
        }

        btnNext.setOnClickListener { activeMediaController?.transportControls?.skipToNext() }
        btnPrev.setOnClickListener { activeMediaController?.transportControls?.skipToPrevious() }

        btnAccept.setOnClickListener {
            val intent = packageManager.getLaunchIntentForPackage("com.android.dialer")
                ?: packageManager.getLaunchIntentForPackage("com.miui.dialer")
            intent?.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent?.let { startActivity(it) }
            collapseToIdle()
        }

        btnDecline.setOnClickListener { collapseToIdle() }
    }

    private fun sendReply(replyText: String) {
        val sbn = NotificationListener.currentNotif ?: return
        val actions = sbn.notification.actions ?: return
        for (action in actions) {
            val remoteInputs = action.remoteInputs ?: continue
            if (remoteInputs.isEmpty()) continue
            val intent = Intent()
            val bundle = Bundle()
            bundle.putCharSequence(remoteInputs[0].resultKey, replyText)
            android.app.RemoteInput.addResultsToIntent(remoteInputs, intent, bundle)
            try {
                action.actionIntent.send(this, 0, intent)
            } catch (e: PendingIntent.CanceledException) {
                e.printStackTrace()
            }
            return
        }
    }

    private fun showNotification(title: String, text: String, pkg: String, key: String) {
        cancelAutoHide()
        try {
            val appInfo = packageManager.getApplicationInfo(pkg, 0)
            val icon = packageManager.getApplicationIcon(appInfo)
            notifIcon.setImageDrawable(icon)
            notifApp.text = packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            notifApp.text = pkg
        }
        notifTitle.text = title
        notifText.text = text
        replyBox.visibility = View.GONE
        switchState(notifView)
        scheduleAutoHide(6000)
    }

    private fun showMusic(title: String, artist: String, isPlaying: Boolean) {
        cancelAutoHide()
        musicTitle.text = title
        musicArtist.text = artist
        btnPlayPause.text = if (isPlaying) "⏸" else "▶"
        switchState(musicView)
    }

    private fun showCall(caller: String) {
        cancelAutoHide()
        callerName.text = caller
        switchState(callView)
    }

    private fun showCharging(level: Int) {
        cancelAutoHide()
        chargingLevel.text = "Battery at $level%"
        switchState(chargingView)
        scheduleAutoHide(5000)
    }

    private fun switchState(showView: View) {
        overlayView.post {
            listOf(idleView, notifView, musicView, callView, chargingView).forEach {
                it.visibility = View.GONE
            }
            showView.visibility = View.VISIBLE
            animateIsland()
        }
    }

    private fun animateIsland() {
        overlayView.scaleX = 0.85f
        overlayView.scaleY = 0.85f
        overlayView.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(350)
            .setInterpolator(OvershootInterpolator(2f))
            .start()
    }

    fun collapseToIdle() {
        cancelAutoHide()
        overlayView.post {
            listOf(notifView, musicView, callView, chargingView).forEach {
                it.visibility = View.GONE
            }
            idleView.visibility = View.VISIBLE
            overlayView.animate()
                .scaleX(0.9f).scaleY(0.9f).setDuration(150).withEndAction {
                    overlayView.animate().scaleX(1f).scaleY(1f).setDuration(200)
                        .setInterpolator(OvershootInterpolator()).start()
                }.start()
        }
    }

    private fun scheduleAutoHide(delayMs: Long) {
        autoHideRunnable = Runnable { collapseToIdle() }
        handler.postDelayed(autoHideRunnable!!, delayMs)
    }

    private fun cancelAutoHide() {
        autoHideRunnable?.let { handler.removeCallbacks(it) }
    }

    private fun startMusicWatcher() {
        try {
            val msm = getSystemService(MEDIA_SESSION_SERVICE) as MediaSessionManager
            NotificationListener.instance ?: return
            val controllers = msm.getActiveSessions(
                ComponentName(this, NotificationListener::class.java)
            )
            if (controllers.isNotEmpty()) attachMediaController(controllers[0])
            msm.addOnActiveSessionsChangedListener({ sessions ->
                if (!sessions.isNullOrEmpty()) attachMediaController(sessions[0])
                else collapseToIdle()
            }, ComponentName(this, NotificationListener::class.java))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun attachMediaController(controller: MediaController) {
        activeMediaController = controller
        val meta = controller.metadata
        val title = meta?.getString(android.media.MediaMetadata.METADATA_KEY_TITLE) ?: return
        val artist = meta.getString(android.media.MediaMetadata.METADATA_KEY_ARTIST) ?: ""
        val isPlaying = controller.playbackState?.state == PlaybackState.STATE_PLAYING
        showMusic(title, artist, isPlaying)

        controller.registerCallback(object : MediaController.Callback() {
            override fun onMetadataChanged(metadata: android.media.MediaMetadata?) {
                val t = metadata?.getString(android.media.MediaMetadata.METADATA_KEY_TITLE) ?: return
                val a = metadata.getString(android.media.MediaMetadata.METADATA_KEY_ARTIST) ?: ""
                showMusic(t, a, true)
            }
            override fun onPlaybackStateChanged(state: PlaybackState?) {
                val playing = state?.state == PlaybackState.STATE_PLAYING
                overlayView.post { btnPlayPause.text = if (playing) "⏸" else "▶" }
            }
        })
    }

    private fun registerReceivers() {
        val filter = IntentFilter().apply {
            addAction(ACTION_SHOW_NOTIFICATION)
            addAction(ACTION_SHOW_CALL)
        }
        registerReceiver(notifReceiver, filter, RECEIVER_NOT_EXPORTED)
        val chargingFilter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        registerReceiver(chargingReceiver, chargingFilter, RECEIVER_NOT_EXPORTED)
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelAutoHide()
        if (::overlayView.isInitialized) windowManager.removeView(overlayView)
        unregisterReceiver(notifReceiver)
        unregisterReceiver(chargingReceiver)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}