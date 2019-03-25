package com.aayush.viasight.util.service

import android.content.Context
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.aayush.viasight.util.INTENT_ACTION_NOTIFICATION
import timber.log.Timber


class NotificationService: NotificationListenerService() {
    private lateinit var context: Context

    override fun onCreate() {
        super.onCreate()

        context = applicationContext
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val notification = sbn?.notification
        val packageName = sbn?.packageName
        val ticker = notification?.tickerText.toString()
        val extras = notification?.extras
        val title = extras?.getString("android.title")
        val text = extras?.getCharSequence("android.text")!!.toString()

        Timber.i(packageName)
        Timber.i(ticker)
        Timber.i(title)
        Timber.i(text)

        val receivedMessage = Intent(INTENT_ACTION_NOTIFICATION)
        receivedMessage.putExtra("package", packageName)
        receivedMessage.putExtra("ticker", ticker)
        receivedMessage.putExtra("title", title)
        receivedMessage.putExtra("text", text)

        LocalBroadcastManager.getInstance(context)
            .sendBroadcast(receivedMessage)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        Timber.i("Notification Removed")
    }
}