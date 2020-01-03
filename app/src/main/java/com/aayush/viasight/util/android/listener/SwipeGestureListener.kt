package com.aayush.viasight.util.android.listener

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import com.aayush.viasight.util.common.MAX_SWIPE_DISTANCE_X
import com.aayush.viasight.util.common.MIN_SWIPE_DISTANCE_X
import com.aayush.viasight.view.MainActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.absoluteValue

class SwipeGestureListener(private var parentContext: Context): GestureDetector.SimpleOnGestureListener() {
    private var swipedOnce = false

    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        val deltaX: Float = e1.x - e2.x
        val deltaXAbs: Float = deltaX.absoluteValue

        if (deltaXAbs in MIN_SWIPE_DISTANCE_X..MAX_SWIPE_DISTANCE_X) {
            when {
                deltaX > 0 -> (parentContext as MainActivity).initSpeechRecognition()
                deltaX < 0 -> if (swipedOnce) {
                    swipedOnce = false
                    (parentContext as MainActivity).stopReadingNotifications()
                }
                else {
                    swipedOnce = true
                    (parentContext as MainActivity).readNotifications()
                }
            }
        }

        return true
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        (parentContext as MainActivity).scrollView.smoothScrollBy(distanceX.toInt(), distanceY.toInt())
        return true
    }
}