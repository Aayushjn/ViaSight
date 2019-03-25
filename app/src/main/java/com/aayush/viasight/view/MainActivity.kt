package com.aayush.viasight.view

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GestureDetectorCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.aayush.viasight.R
import com.aayush.viasight.model.NotificationInfo
import com.aayush.viasight.util.EXTRA_NOTIFICATION
import com.aayush.viasight.util.INTENT_ACTION_NOTIFICATION
import com.aayush.viasight.util.UTTERANCE_ID_NOTIFICATION
import com.aayush.viasight.util.listener.SwipeGestureListener
import timber.log.Timber
import java.util.*

class MainActivity: AppCompatActivity() {
    private var notifications = mutableListOf<NotificationInfo>()
    private var tts: TextToSpeech? = null
    private lateinit var gestureDetector: GestureDetectorCompat

    private val onNotice = object: BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val notificationInfo = intent.getParcelableExtra<NotificationInfo>(EXTRA_NOTIFICATION)
            notifications.add(notificationInfo)

            Timber.d(notificationInfo.toString())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val gestureListener = SwipeGestureListener()
        gestureListener.setActivity(this)
        gestureDetector = GestureDetectorCompat(this, gestureListener)
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(onNotice, IntentFilter(INTENT_ACTION_NOTIFICATION))
    }

    override fun onPause() {
        super.onPause()

        tts?.let {
            if (it.isSpeaking) {
                it.stop()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        tts?.shutdown()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        gestureDetector.onTouchEvent(event)
        return true
    }

    fun initAudioRecord() {

    }

    fun readNotifications() {
        if (!notifications.isEmpty()) {
            notifications.forEach { if (it.isRead) notifications.remove(it) }
            tts = TextToSpeech(this, TextToSpeech.OnInitListener {
                if (it == TextToSpeech.SUCCESS) {
                    val result = this.tts?.setLanguage(Locale.ENGLISH)
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(this, "This Language is not supported", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        for (notification in notifications) {
                            tts?.speak(notification.toString(), TextToSpeech.QUEUE_FLUSH, null,
                                UTTERANCE_ID_NOTIFICATION)
                            notification.isRead = true
                        }
                    }
                }
                else {
                    Timber.e("Initialization failed")
                }
            })
        }
    }

    fun stopReadingNotifications() {
        tts?.let {
            if (it.isSpeaking) {
                it.stop()
            }
        }
    }
}
