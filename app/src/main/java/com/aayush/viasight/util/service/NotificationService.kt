package com.aayush.viasight.util.service

import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Vibrator
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.aayush.viasight.model.NotificationInfo
import com.aayush.viasight.util.EXTRA_NOTIFICATION
import com.aayush.viasight.util.INTENT_ACTION_NOTIFICATION
import com.aayush.viasight.util.NOTIFICATION_WAVEFORM
import com.aayush.viasight.util.vibrate
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
            notification?.`when`?.let { Date(it) } ?: sbn?.postTime?.let { Date(it) } ?: Date()
        )

        if (notificationInfo.packageName != "Unknown package" && notificationInfo.title != "Unknown title" &&
            notificationInfo.text != "Unknown text") {
            val receivedNotification = Intent(INTENT_ACTION_NOTIFICATION)
            receivedNotification.putExtra(EXTRA_NOTIFICATION, notificationInfo)

            LocalBroadcastManager.getInstance(applicationContext)
                .sendBroadcast(receivedNotification)

            vibrate(getSystemService(Context.VIBRATOR_SERVICE) as Vibrator, NOTIFICATION_WAVEFORM)
            val ringtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            RingtoneManager.getRingtone(applicationContext, ringtone).play()
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        Timber.d("${sbn.packageName} removed")
    }
}