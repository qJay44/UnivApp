package edu.muiv.univapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.annotation.StringRes
import edu.muiv.univapp.api.CoreDatabaseFetcher
import edu.muiv.univapp.database.UnivRepository
import edu.muiv.univapp.utils.CodeInspectionHelper
import java.io.File

class UnivApplication : Application() {
    companion object {
        const val SCHEDULE_CHANNEL_ID = "schedulePoll"
        const val UNIV_NOTIFICATION_CHANNEL_ID = "univNotificationPoll"
    }

    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler(CodeInspectionHelper.CustomizedExceptionHandler(
            filesDir.absolutePath + File.separator
        ))
        UnivRepository.initialize(this)
        CoreDatabaseFetcher.initialize()

        createChannel(R.string.schedule_channel_name, SCHEDULE_CHANNEL_ID)
        createChannel(R.string.univ_notification_channel_name, UNIV_NOTIFICATION_CHANNEL_ID)
    }

    private fun createChannel(@StringRes name: Int, id: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = getString(name)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(id, channelName, importance)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
