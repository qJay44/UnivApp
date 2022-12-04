package edu.muiv.univapp.utils

import android.content.Context
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import androidx.core.view.GestureDetectorCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
import kotlin.math.abs


class OnTouchListenerItem (
    context: Context, recyclerView: RecyclerView,
    private val onTouchActionListener: OnTouchActionListener
) : OnItemTouchListener {

    companion object {
        private const val SWIPE_MIN_DISTANCE = 120
        private const val SWIPE_THRESHOLD_VELOCITY = 200
        private const val SWIPE_MAX_OFF_PATH = 250
    }

    interface OnTouchActionListener {
        fun onLeftSwipe(view: View, position: Int)
        fun onRightSwipe(view: View, position: Int)
        fun onClick(view: View, position: Int)
    }

    private val gestureDetector: GestureDetectorCompat

    init {
        gestureDetector = GestureDetectorCompat(context, object : SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                // Find the item view that was swiped based on the coordinates
                val child = recyclerView.findChildViewUnder(e.x, e.y)
                child?.let {
                    val childPosition = recyclerView.getChildAdapterPosition(child)
                    onTouchActionListener.onClick(child, childPosition)
                }

                return false
            }

            override fun onFling(
                e1: MotionEvent, e2: MotionEvent,
                velocityX: Float, velocityY: Float
            ): Boolean {
                try {
                    if (abs(e1.y - e2.y) > SWIPE_MAX_OFF_PATH) {
                        return false
                    }

                    // Find the item view that was swiped based on the coordinates
                    val child = recyclerView.findChildViewUnder(e1.x, e1.y)
                    child?.let {
                        val childPosition = recyclerView.getChildAdapterPosition(child)

                        // right to left swipe
                        if (e1.x - e2.x > SWIPE_MIN_DISTANCE && abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                            onTouchActionListener.onLeftSwipe(child, childPosition)
                        } else if (e2.x - e1.x > SWIPE_MIN_DISTANCE && abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                            onTouchActionListener.onRightSwipe(child, childPosition)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return false
            }
        })
    }

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(e)
        return false
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
}