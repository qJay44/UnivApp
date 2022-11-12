package edu.muiv.univapp.schedule

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import edu.muiv.univapp.R

class ScheduleActivity : AppCompatActivity(), ScheduleListFragment.Callbacks {

    private companion object {
        private const val TAG = "ScheduleActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val userBundle = intent.getBundleExtra("userBundle")!!
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

        if (currentFragment == null) {
            Log.i(TAG, "Creating fragment list...")
            val fragment = ScheduleListFragment.newInstance(userBundle)
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, fragment)
                .commit()

        }
    }

    override fun onScheduleDaySelect(scheduleDate: String) {
        val fragment = ScheduleFragment.newInstance(scheduleDate)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack("scheduleDay")
            .commit()
    }
}