package edu.muiv.univapp.ui.navigation.schedule.utils

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

open class OnTouchListenerRecyclerView(context: Context?, private val recyclerView: RecyclerView)
    : View.OnTouchListener {

    companion object {
        private const val TAG = "OnSwipeTouchListener"
        private const val SWIPE_THRESHOLD = 100
        private const val SWIPE_THRESHOLD_VELOCITY = 100
    }

    private val gestureDetector = GestureDetector(context, GestureListener())

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        Log.i(TAG, event.action.toString())
        return gestureDetector.onTouchEvent(event)
    }

    open fun onSwipeRight(): Boolean { return false }
    open fun onSwipeLeft() : Boolean { return false }
    open fun onClick(view: View, position: Int): Boolean { return false }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            // Find the item view that was swiped based on the coordinates
            val child = recyclerView.findChildViewUnder(e.x, e.y)
            child?.let {
                val childPosition = recyclerView.getChildAdapterPosition(child)
                onClick(child, childPosition)
            }

            return false
        }

        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float,
        ): Boolean {
            try {
                val diffX = e2.x - e1.x
                return if (abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    when {
                        diffX > SWIPE_THRESHOLD -> onSwipeRight()
                        diffX < -SWIPE_THRESHOLD -> onSwipeLeft()
                        else -> false
                    }
                } else false
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
            return false
        }
    }
}
