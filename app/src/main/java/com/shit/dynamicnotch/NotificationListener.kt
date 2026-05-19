package com.yourname.dynamicnotch

import android.app.Notification
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val extras = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) ?: return
        val text = extras.getString(Notification.EXTRA_TEXT) ?: ""

        // Skip our own notification
        if (sbn.packageName == packageName) return

        val intent = Intent(OverlayService.ACTION_SHOW_NOTIFICATION).apply {
            putExtra(OverlayService.EXTRA_TITLE, title)
            putExtra(OverlayService.EXTRA_TEXT, text)
            putExtra(OverlayService.EXTRA_APP, sbn.packageName)
        }
        sendBroadcast(intent)
    }
}