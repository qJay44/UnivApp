package edu.muiv.univapp.ui.navigation.schedule.utils

import android.util.Log
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener

class WeekChangeAnimationListener(private val loadWeek: () -> Unit) : AnimationListener {

    companion object {
        private lateinit var TAG: String
        private var BIND_FUNC: (() -> Unit?)? = null

        var isAnimationEnded = true
            private set

        var isAdapterAttached = false
            private set

        var isAdapterUpdated = false

        fun setBindFunc(tag: String, bindFunc: () -> Unit) {
            if (BIND_FUNC == null) {
                TAG = tag
                BIND_FUNC = bindFunc
            } else {
                Log.w(TAG, "Binding function already initialized")
            }
        }

        fun resetListener() {
            BIND_FUNC = null
            isAdapterAttached = false
            isAnimationEnded = true
            isAdapterUpdated = false
        }
    }

    override fun onAnimationStart(p0: Animation?) {
        isAdapterUpdated = false
        isAnimationEnded = false
        isAdapterAttached = false
        loadWeek()
    }

    override fun onAnimationEnd(p0: Animation?) {
        if (isAdapterUpdated) {
            BIND_FUNC!!.invoke() ?: throw IllegalStateException("Binding function must be initialized")
            isAdapterAttached = true
        }
        isAnimationEnded = true
    }

    override fun onAnimationRepeat(p0: Animation?) {}
}
