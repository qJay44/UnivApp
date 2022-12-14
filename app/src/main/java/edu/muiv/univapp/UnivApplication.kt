package edu.muiv.univapp

import android.app.Application
import edu.muiv.univapp.api.ExternalDatabaseFetcher
import edu.muiv.univapp.database.UnivRepository
import edu.muiv.univapp.utils.CodeInspectionHelper
import java.io.File

class UnivApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler(CodeInspectionHelper.CustomizedExceptionHandler(
            filesDir.absolutePath + File.separator
        ))
        UnivRepository.initialize(this)
        ExternalDatabaseFetcher.initialize()
    }
}
