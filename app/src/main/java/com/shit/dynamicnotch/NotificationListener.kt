package com.yourname.dynamicnotch

import android.app.Notification
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotificationListener : NotificationListenerService() {

    // Merge everything into ONE companion object
    companion object {
        var instance: NotificationListener? = null
        var currentNotif: StatusBarNotification? = null
    }

    override fun onListenerConnected() {
        instance = this
    }

    override fun onListenerDisconnected() {
        instance = null
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // Don't show notifications from our own app
        if (sbn.packageName == packageName) return

        val extras = sbn.notification.extras
        
        // FIXED TYPO: Changed 'Notitfication' to 'Notification'
        val title = extras.getString(Notification.EXTRA_TITLE) ?: return
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

        // Sending the broadcast to the OverlayService
        val intent = Intent(OverlayService.ACTION_SHOW_NOTIFICATION).apply {
            putExtra(OverlayService.EXTRA_TITLE, title)
            putExtra(OverlayService.EXTRA_TEXT, text)
            putExtra(OverlayService.EXTRA_PACKAGE, sbn.packageName)
            putExtra(OverlayService.EXTRA_NOTIF_KEY, sbn.key)
            // Ensure this matches your broadcast receiver setup in OverlayService
            setPackage(packageName) 
        }
        sendBroadcast(intent)

        saveCurrentNotif(sbn)
    }

    private fun saveCurrentNotif(sbn: StatusBarNotification) {
        currentNotif = sbn
    }
}