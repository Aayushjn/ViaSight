package com.aayush.viasight.util.listener

import android.view.GestureDetector
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.aayush.viasight.util.MAX_SWIPE_DISTANCE_X
import com.aayush.viasight.util.MIN_SWIPE_DISTANCE_X
import com.aayush.viasight.view.MainActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.absoluteValue

class SwipeGestureListener: GestureDetector.SimpleOnGestureListener() {
    private var swipedOnce = false

    private var activity: AppCompatActivity? = null

    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        val deltaX = e1.x - e2.x
        val deltaXAbs = deltaX.absoluteValue

        if (deltaXAbs in MIN_SWIPE_DISTANCE_X..MAX_SWIPE_DISTANCE_X) {
            if (deltaX > 0) {
                (activity as MainActivity).initSpeechRecognition()
            }
            else if (deltaX < 0) {
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

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        (activity as MainActivity).scrollView.smoothScrollBy(distanceX.toInt(), distanceY.toInt())
        return true
    }

    fun setActivity(activity: AppCompatActivity) {
        this.activity = activity
    }
}