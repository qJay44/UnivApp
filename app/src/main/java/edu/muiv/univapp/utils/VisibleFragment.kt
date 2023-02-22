package edu.muiv.univapp.utils

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.fragment.app.Fragment

abstract class VisibleFragment : Fragment() {
    companion object {
        private const val TAG = "VisibleFragment"
    }

    private val onShowNotification = object : BroadcastReceiver() {
        override fun onReceive(p0: Context, p1: Intent) {
            Log.i(TAG, "onReceive: Canceling notifications")
            resultCode = Activity.RESULT_CANCELED
        }
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(PollWorker.ACTION_SHOW_NOTIFICATION)
        requireActivity().registerReceiver(
            onShowNotification,
            filter,
            PollWorker.PERMISSION_SHOW_NOTIFICATION,
            null
        )
    }

    override fun onStop() {
        super.onStop()
        requireActivity().unregisterReceiver(onShowNotification)
    }
}
