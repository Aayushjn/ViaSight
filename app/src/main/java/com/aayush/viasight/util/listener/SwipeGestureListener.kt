package com.aayush.viasight.util.listener

import android.view.GestureDetector
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.aayush.viasight.util.MAX_SWIPE_DISTANCE_Y
import com.aayush.viasight.util.MIN_SWIPE_DISTANCE_Y
import com.aayush.viasight.view.MainActivity
import kotlin.math.absoluteValue

class SwipeGestureListener: GestureDetector.SimpleOnGestureListener() {
    private var swipedOnce = false

    private var activity: AppCompatActivity? = null

    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        val deltaY = e1.y - e2.y
        val deltaYAbs = deltaY.absoluteValue

        if (deltaYAbs in MIN_SWIPE_DISTANCE_Y..MAX_SWIPE_DISTANCE_Y) {
            if (deltaY > 0) {
                (activity as MainActivity).initSpeechRecognition()
            }
            else if (deltaY < 0) {
                if (swipedOnce) {
                    swipedOnce = false
                    (activity as MainActivity).stopReadingNotifications()
                }
                else {
                    swipedOnce = true
                    (activity as MainActivity).readNotifications()
                }
            }
        }

        return true
    }

    fun setActivity(activity: AppCompatActivity) {
        this.activity = activity
    }
}