package edu.muiv.univapp.utils

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import edu.muiv.univapp.api.CoreDatabaseFetcher
import edu.muiv.univapp.api.LoginResponse
import edu.muiv.univapp.api.StatusCode

class PollWorker(val context: Context, workerParams: WorkerParameters)
    : Worker(context, workerParams) {

    companion object {
        private const val TAG = "PollWorker"
        private const val PREFS_NAME = "USER_INFO"
        private const val LAST_SCHEDULE = "lastSchedule"
    }

    private val userLoaded by lazy { loadUserPrefs() }
    private val univAPI    by lazy { CoreDatabaseFetcher.get() }
    private val listDiff   by lazy { TwoListsDifferenceString() }

    override fun doWork(): Result {
        if (UserDataHolder.isServerOnline && userLoaded) {
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
                    // TODO: Send notification
                    Log.i(TAG, "doWork: Got new schedule")
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
            val gson = Gson()
            val type = object : TypeToken<List<String>?>() {}.type
            gson.fromJson(serializedObject, type)
        } else {
            null
        }
    }
}
