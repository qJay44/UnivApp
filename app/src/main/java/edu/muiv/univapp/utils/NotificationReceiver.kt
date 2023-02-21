package edu.muiv.univapp.utils

import android.Manifest
import android.app.Activity
import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat

class NotificationReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "NotificationReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "onReceive: result: $resultCode")
        if (resultCode != Activity.RESULT_OK) {
            Log.i(TAG, "onReceive: Send notification was canceled")
        }

        val requestCode = intent.getIntExtra(PollWorker.REQUEST_CODE, 0)

        @Suppress("DEPRECATION")
        val notification: Notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(PollWorker.NOTIFICATION, Notification::class.java)!!
        } else {
            intent.getParcelableExtra(PollWorker.NOTIFICATION)!!
        }

        val notificationManager = NotificationManagerCompat.from(context)

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(requestCode, notification)
        } else {
            Log.w(TAG, "checkNotificationPermission: no permission to send notification")
        }
    }
}
