package edu.muiv.univapp.user

import android.os.*
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.size
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import edu.muiv.univapp.R
import edu.muiv.univapp.databinding.ActivityUserBinding

class NavigationActivity : AppCompatActivity() {

    private lateinit var tvStudentName: TextView
    private lateinit var tvStudentGroup: TextView
    // private lateinit var tvStudentCourseAndSemester: TextView

    private lateinit var binding: ActivityUserBinding
    private lateinit var navView: BottomNavigationView
    private lateinit var navController: NavController
    private var pressedOnce = false
    private var selectedItem = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserBinding.inflate(layoutInflater)
        supportActionBar?.hide()
        setContentView(binding.root)

        tvStudentName = binding.tvStudentName
        tvStudentGroup = binding.tvStudentGroup
        // tvStudentCourseAndSemester = binding.tvStudentCourseAndSemester

        val user = UserDataHolder.get().user
        val nameField = "${user.name}\n${user.surname}"

        tvStudentName.text = nameField
        tvStudentGroup.text = user.groupName

        navView = binding.navView
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_user) as NavHostFragment

        navController = navHostFragment.navController

        navView.setOnItemSelectedListener { item ->
            selectFragment(item)
            true
        }

        // Logout on double back //

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (navController.currentDestination?.id == R.id.navigation_schedule) {
                    navController.popBackStack()
                    return
                }
                if (pressedOnce) {
                    UserDataHolder.uninitialize()
                    finish()
                }
                pressedOnce = true

                Toast.makeText(
                    this@NavigationActivity,
                    "Click BACK again to exit",
                    Toast.LENGTH_SHORT
                ).show()

                Handler(Looper.myLooper()!!).postDelayed({ pressedOnce = false }, 2000)
            }
        }
        )

        ///////////////////////////
    }

    override fun onDestroy() {
        super.onDestroy()
        UserDataHolder.uninitialize()
    }

    private fun selectFragment(item: MenuItem) {
        if (selectedItem == -1) {
            navController.navigate(item.itemId)
        } else {
            navController.navigate(
                when (item.itemId) {
                    R.id.navigation_notifications -> {
                        R.id.action_global_navigation_notifications
                    }
                    R.id.navigation_schedule_list -> {
                        R.id.navigation_schedule_list
                    }
                    R.id.navigation_profile -> {
                        R.id.action_global_navigation_profile
                    }
                    else -> item.itemId
                }
            )
        }
        selectedItem = item.itemId

        for (i in 0 until navView.size) {
            val menuItem = navView.menu.getItem(i)
            if (menuItem.itemId == item.itemId) menuItem.isChecked = true
        }
    }
}
