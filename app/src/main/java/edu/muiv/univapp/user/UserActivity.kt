package edu.muiv.univapp.user

import android.os.*
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import edu.muiv.univapp.R
import edu.muiv.univapp.databinding.ActivityUserBinding

class UserActivity : AppCompatActivity() {

    private lateinit var tvStudentName: TextView
    private lateinit var tvStudentGroup: TextView
    // private lateinit var tvStudentCourseAndSemester: TextView

    private lateinit var binding: ActivityUserBinding
    // private var pressedOnce = false

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

        val navView: BottomNavigationView = binding.navView
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_user) as NavHostFragment

        val navController = navHostFragment.navController
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_notifications, R.id.navigation_schedule_list
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Logout on double back //

//        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//                if (pressedOnce) {
//                    UserDataHolder.uninitialize()
//                    finish()
//                }
//                pressedOnce = true
//
//                Toast.makeText(
//                    this@UserActivity,
//                    "Click BACK again to exit",
//                    Toast.LENGTH_SHORT
//                ).show()
//
//                Handler(Looper.myLooper()!!).postDelayed({ pressedOnce = false }, 2000)
//            }
//        }
//        )

        ///////////////////////////
    }

    override fun onDestroy() {
        super.onDestroy()
        UserDataHolder.uninitialize()
    }
}
