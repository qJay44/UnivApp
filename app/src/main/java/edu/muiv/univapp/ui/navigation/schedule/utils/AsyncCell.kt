package edu.muiv.univapp.ui.navigation.schedule.utils

import android.content.Context
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.asynclayoutinflater.view.AsyncLayoutInflater

open class AsyncCell(context: Context) : FrameLayout(context, null, 0, 0) {
    init {
        layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    }

    // Override with layout ID
    open val layoutId = -1
    private var isInflated = false
    private val bindingFunctions: MutableList<AsyncCell.() -> Unit> = mutableListOf()

    private fun bindView() {
        with(bindingFunctions) {
            forEach { it() }
            clear()
        }
    }

    fun inflate() {
        AsyncLayoutInflater(context).inflate(layoutId, this) { view, _, _ ->
            isInflated = true
            addView(view)
            bindView()
        }
    }

    fun bindWhenInflated(bindFunc: AsyncCell.() -> Unit) {
        if (isInflated)
            bindFunc()
        else
            bindingFunctions.add(bindFunc)
    }
}
