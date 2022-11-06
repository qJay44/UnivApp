package edu.muiv.univapp

import android.app.Application
import edu.muiv.univapp.database.UnivRepository

class UnivApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        UnivRepository.initialize(this)
    }
}