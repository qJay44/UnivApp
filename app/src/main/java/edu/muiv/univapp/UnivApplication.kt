package edu.muiv.univapp

import android.app.Application
import edu.muiv.univapp.database.UnivRepository
import edu.muiv.univapp.utils.CodeInspectionHelper

class UnivApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler(CodeInspectionHelper.CustomizedExceptionHandler())
        UnivRepository.initialize(this)
    }
}