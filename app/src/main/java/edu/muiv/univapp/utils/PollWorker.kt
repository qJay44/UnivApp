package edu.muiv.univapp.utils

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import edu.muiv.univapp.R
import edu.muiv.univapp.UnivApplication
import edu.muiv.univapp.api.CoreDatabaseFetcher
import edu.muiv.univapp.api.LoginResponse
import edu.muiv.univapp.api.StatusCode
import edu.muiv.univapp.ui.login.LoginActivity
import edu.muiv.univapp.ui.login.LoginResult
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PollWorker(private val context: Context, workerParams: WorkerParameters)
    : Worker(context, workerParams) {

    companion object {
        private const val TAG = "PollWorker"
        private const val PREFS_NAME = "USER_INFO"
        private const val LAST_SCHEDULE = "lastSchedule"
        private const val LAST_UNIV_NOTIFICATION = "lastUnivNotification"

        const val ACTION_SHOW_NOTIFICATION = "edu.muiv.univapp.utils.SHOW_NOTIFICATION"
        const val PERMISSION_SHOW_NOTIFICATION = "edu.muiv.univapp.PRIVATE"
        const val REQUEST_CODE = "requestCode"
        const val NOTIFICATION = "notification"
    }

    private val calendar = Calendar.getInstance()
    private val userLoaded by lazy { loadUserPrefs() }
    private val univAPI    by lazy { CoreDatabaseFetcher.get() }
    private val listDiff   by lazy { TwoStringListsDifference() }
    private val originalDateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.FRANCE)
    private val days: Array<String> = Array(7) { it.toString() }

    override fun doWork(): Result {
        if (UserDataHolder.isInternetAvailable && userLoaded) {
            val user = UserDataHolder.get().user

            createScheduleNotification(user)
            createUnivNotificationNotification(user)
        }

        return Result.success()
    }

    private fun loadDays() {
        for (i in days.indices) {
            days[i] = originalDateFormat.format(calendar.time)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        // Subtract extra added day
        calendar.add(Calendar.DAY_OF_MONTH, -1)
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun createScheduleNotification(user: LoginResult) {
        loadDays()
        listDiff.oldList = getPrefs(LAST_SCHEDULE, "schedule") ?: emptyList()

        if (user.groupName != null) {
            univAPI.fetchSchedule(
                group = user.groupName,
                dateStart = days.first(),
                dateEnd = days.last()
            ) { response ->
                val statusCode = response.keys.first()
                val scheduleList = response.values.first()

                if (statusCode == StatusCode.OK)
                    listDiff.newList = scheduleList!!.map { it.id }
            }
        } else {
            univAPI.fetchSchedule(
                teacherId = user.id,
                dateStart = days.first(),
                dateEnd = days.last()
            ) { response ->
                val statusCode = response.keys.first()
                val scheduleList = response.values.first()

                if (statusCode == StatusCode.OK)
                    listDiff.newList = scheduleList!!.map { it.id }
            }
        }

        try {
            if (listDiff.compareLists().isNotEmpty()) {
                Log.i(TAG, "doWork: Got a new schedule")

                val intent = LoginActivity.newIntent(context)
                val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT)
                val resources = context.resources

                val notification = NotificationCompat
                    .Builder(context, UnivApplication.UNIV_NOTIFICATION_CHANNEL_ID)
                    .setTicker(resources.getString(R.string.new_univ_notification_title))
                    .setSmallIcon(android.R.drawable.ic_menu_report_image)
                    .setContentTitle(resources.getString(R.string.new_univ_notification_title))
                    .setContentText(resources.getString(R.string.new_univ_notification_text))
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build()

                showBackgroundNotification(notification)
            } else {
                Log.i(TAG, "doWork: No new schedule")
            }
        } catch (_: UninitializedPropertyAccessException) {
            Log.i(TAG, "doWork: No new schedule (catch block)")
        }

    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun createUnivNotificationNotification(user: LoginResult) {
        listDiff.oldList = getPrefs(LAST_UNIV_NOTIFICATION, "univNotification") ?: emptyList()
        Log.i(TAG, "createScheduleNotification: oldList: ${listDiff.oldList}")

        if (user.groupName != null)
            univAPI.fetchNotifications(user.groupName) { response ->
                val statusCode = response.keys.first()
                val notificationList = response.values.first()

                if (statusCode == StatusCode.OK)
                    listDiff.newList = notificationList!!.map { it.id }
            }

        try {
            if (listDiff.compareLists().isNotEmpty()) {
                Log.i(TAG, "doWork: Got new notifications")

                val intent = LoginActivity.newIntent(context)
                val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT)
                val resources = context.resources
                val notification = NotificationCompat
                    .Builder(context, UnivApplication.SCHEDULE_CHANNEL_ID)
                    .setTicker(resources.getString(R.string.new_schedule_title))
                    .setSmallIcon(android.R.drawable.ic_menu_report_image)
                    .setContentTitle(resources.getString(R.string.new_schedule_title))
                    .setContentText(resources.getString(R.string.new_schedule_text))
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build()

                showBackgroundNotification(notification)
            } else {
                Log.i(TAG, "doWork: No new notifications")
            }
        } catch (_: UninitializedPropertyAccessException) {
            Log.i(TAG, "doWork: No new notifications (catch block)")
        }
    }

    private fun loadUserPrefs(): Boolean {
        val settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) ?: return false
        val userInfoAsString = settings.getString("userInfo", null)
        val userInfo = Gson().fromJson(userInfoAsString, LoginResponse::class.java) ?: return false
        UserDataHolder.initialize(userInfo)

        return true
    }

    private fun getPrefs(prefName: String, keyName: String): List<String>? {
        val settings = context.getSharedPreferences(prefName, Context.MODE_PRIVATE) ?: return null
        val serializedObject = settings.getString(keyName, null)

        return if (serializedObject != null) {
            val type = object : TypeToken<List<String>?>() {}.type
            Gson().fromJson(serializedObject, type)
        } else {
            null
        }
    }

    private fun showBackgroundNotification(notification: Notification) {
        val intent = Intent(ACTION_SHOW_NOTIFICATION).apply {
            putExtra(REQUEST_CODE, 0)
            putExtra(NOTIFICATION, notification)
        }

        context.sendOrderedBroadcast(intent, PERMISSION_SHOW_NOTIFICATION)
    }
}
