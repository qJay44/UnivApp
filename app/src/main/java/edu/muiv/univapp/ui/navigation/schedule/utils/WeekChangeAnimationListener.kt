package edu.muiv.univapp.ui.navigation.schedule.utils

import android.util.Log
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener

class WeekChangeAnimationListener(private val loadWeek: () -> Unit) : AnimationListener {

    companion object {
        private var BIND_FUNC: (() -> Unit?)? = null

        fun setBindFunc(tag: String, bindFunc: () -> Unit) {
            if (BIND_FUNC == null)
                BIND_FUNC = bindFunc
            else
                Log.w(tag, "Binding function already initialized")
        }
    }

    override fun onAnimationStart(p0: Animation?) {
        loadWeek()
    }

    override fun onAnimationEnd(p0: Animation?) {
        BIND_FUNC!!.invoke() ?: throw IllegalStateException("Binding function must be initialized")
    }

    override fun onAnimationRepeat(p0: Animation?) {}
}
