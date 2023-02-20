package edu.muiv.univapp.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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

class PollWorker(private val context: Context, workerParams: WorkerParameters)
    : Worker(context, workerParams) {

    companion object {
        private const val TAG = "PollWorker"
        private const val PREFS_NAME = "USER_INFO"
        private const val LAST_SCHEDULE = "lastSchedule"
    }

    private val userLoaded by lazy { loadUserPrefs() }
    private val univAPI    by lazy { CoreDatabaseFetcher.get() }
    private val listDiff   by lazy { TwoListsDifferenceString() }

    @SuppressLint("UnspecifiedImmutableFlag")
    override fun doWork(): Result {
        if (UserDataHolder.isServerOnline && userLoaded && checkNotificationsPermission()) {
            val user = UserDataHolder.get().user

            listDiff.oldList = getSchedulePrefs() ?: emptyList()

            if (user.groupName != null) {
                univAPI.fetchSchedule(group = user.groupName) { response ->
                    val statusCode = response.keys.first()
                    val scheduleList = response.values.first()

                    if (statusCode == StatusCode.OK)
                        listDiff.newList = scheduleList!!.map { it.id }
                }
            } else {
                univAPI.fetchSchedule(teacherId = user.id) { response ->
                    val statusCode = response.keys.first()
                    val scheduleList = response.values.first()

                    if (statusCode == StatusCode.OK)
                        listDiff.newList = scheduleList!!.map { it.id }
                }
            }

            try {
                if (listDiff.deleteList.isNotEmpty()) {
                    Log.i(TAG, "doWork: Got a new schedule")

                    val intent = LoginActivity.newIntent(context)
                    val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT)
                    val resources = context.resources
                    val notification = NotificationCompat
                        .Builder(context, UnivApplication.NOTIFICATION_CHANNEL_ID)
                        .setTicker(resources.getString(R.string.new_schedule_title))
                        .setSmallIcon(android.R.drawable.ic_menu_report_image)
                        .setContentTitle(resources.getString(R.string.new_schedule_title))
                        .setContentText(resources.getString(R.string.new_schedule_text))
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .build()

                    val notificationManager = NotificationManagerCompat.from(context)

                    notificationManager.notify(0, notification)
                } else {
                    Log.i(TAG, "doWork: No new schedule")
                }
            } catch (_: UninitializedPropertyAccessException) {
                Log.i(TAG, "doWork: No new schedule (catch block)")
            }
        }

        return Result.success()
    }

    private fun loadUserPrefs(): Boolean {
        val settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) ?: return false
        val userInfoAsString = settings.getString("userInfo", null)
        val userInfo = Gson().fromJson(userInfoAsString, LoginResponse::class.java) ?: return false
        UserDataHolder.initialize(userInfo)

        return true
    }

    private fun getSchedulePrefs(): List<String>? {
        val settings = context.getSharedPreferences(LAST_SCHEDULE, Context.MODE_PRIVATE) ?: return null
        val serializedObject = settings.getString("schedule", null)

        return if (serializedObject != null) {
            val type = object : TypeToken<List<String>?>() {}.type
            Gson().fromJson(serializedObject, type)
        } else {
            null
        }
    }

    private fun checkNotificationsPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED)
        } else {
            true
        }
    }
}
