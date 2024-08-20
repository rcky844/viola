package tipz.viola.webview.activity.components

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View

class SwipeController(private val direction: Int,
                      private val callback: () -> Any) : View.OnTouchListener {
    private val LOG_TAG = "SwipeController"

    /* Initial position */
    private var x1 = 0f
    private var y1 = 0f

    /* Final position */
    private var x2 = 0f
    private var y2 = 0f

    /* Parameters */
    private var swipeThreshold = 500f

    private fun onTrackingStart() {
        Log.d(LOG_TAG, "onTrackingStart(): x1=$x1, y1=$y1")
    }

    private fun onTrackingEnd(v: View) {
        Log.d(LOG_TAG, "onTrackingEnd(): x2=$x2, y1=$y2")

        val triggered: Boolean = when (direction) {
            DIRECTION_SWIPE_UP -> {
                (y1 - y2) > swipeThreshold
            }
            DIRECTION_SWIPE_DOWN -> {
                (y2 - y1) > swipeThreshold
            }
            else -> {
                Log.d(LOG_TAG, "onTrackingEnd(): Invalid direction=$direction")
                false
            }
        }

        if (triggered) {
            callback()
            v.performHapticFeedback(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                    HapticFeedbackConstants.GESTURE_END
                else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    HapticFeedbackConstants.CONTEXT_CLICK
                else HapticFeedbackConstants.KEYBOARD_TAP
            )
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            /* Item click start */
            MotionEvent.ACTION_DOWN -> {
                x1 = event.x
                y1 = event.y
                onTrackingStart()
            }

            /* Item click end */
            MotionEvent.ACTION_UP -> {
                x2 = event.x
                y2 = event.y
                onTrackingEnd(v)
            }
        }
        return false
    }

    companion object {
        const val DIRECTION_SWIPE_UP = 0
        const val DIRECTION_SWIPE_DOWN = 1
    }
}