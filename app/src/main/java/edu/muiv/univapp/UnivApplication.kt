package edu.muiv.univapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import edu.muiv.univapp.api.CoreDatabaseFetcher
import edu.muiv.univapp.database.UnivRepository
import edu.muiv.univapp.utils.CodeInspectionHelper
import java.io.File

class UnivApplication : Application() {
    companion object {
        const val NOTIFICATION_CHANNEL_ID = "schedulePoll"
    }
    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler(CodeInspectionHelper.CustomizedExceptionHandler(
            filesDir.absolutePath + File.separator
        ))
        UnivRepository.initialize(this)
        CoreDatabaseFetcher.initialize()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_name)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
