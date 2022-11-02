package edu.muiv.univapp

import android.app.Application

class UnivAppApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ScheduleRepository.initialize(this)
    }
}