package com.aayush.viasight.util.service

import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.aayush.viasight.model.NotificationInfo
import com.aayush.viasight.util.EXTRA_NOTIFICATION
import com.aayush.viasight.util.INTENT_ACTION_NOTIFICATION
import timber.log.Timber
import java.util.*


class NotificationService: NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val notification = sbn?.notification
        val extras = notification?.extras

        val notificationInfo = NotificationInfo(
            sbn?.packageName ?: "Unknown package",
            notification?.tickerText.toString(),
            extras?.getString("android.title") ?: "Unknown title",
            extras?.getString("android.text") ?: "Unknown text",
            notification?.`when` ?: sbn?.postTime ?: Date().time
        )

        val receivedNotification = Intent(INTENT_ACTION_NOTIFICATION)
        receivedNotification.putExtra(EXTRA_NOTIFICATION, notificationInfo)

        LocalBroadcastManager.getInstance(applicationContext)
            .sendBroadcast(receivedNotification)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        Timber.d("${sbn.packageName} removed")
    }
}