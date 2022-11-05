package edu.muiv.univapp

import android.app.Application

class UnivApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ScheduleRepository.initialize(this)
    }
}