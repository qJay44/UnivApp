package edu.muiv.univapp.ui.navigation

import android.content.Context
import android.os.*
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.gson.Gson
import edu.muiv.univapp.R
import edu.muiv.univapp.databinding.ActivityNavigationBinding
import edu.muiv.univapp.utils.UserDataHolder

class NavigationActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "NavigationActivity"
        private const val PREFS_NAME = "USER_INFO"
    }

    private lateinit var tvStudentName: TextView
    private lateinit var tvStudentGroup: TextView
    private lateinit var tvStudentCourseAndSemester: TextView

    private lateinit var binding: ActivityNavigationBinding
    private lateinit var navView: BottomNavigationView
    private lateinit var navController: NavController

    private var pressedOnce = false
    private var selectedItem = R.id.navigation_schedule_list
    private var isLoggingOut = false

    private val navigationViewModel: NavigationViewModel by lazy {
        ViewModelProvider(this)[NavigationViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tvStudentName = binding.tvStudentName
        tvStudentGroup = binding.tvStudentGroup
        tvStudentCourseAndSemester = binding.tvStudentCourseAndSemester

        tvStudentName.text = navigationViewModel.nameField
        tvStudentGroup.text = navigationViewModel.groupName
        tvStudentCourseAndSemester.text = navigationViewModel.studyInfo

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
                    isLoggingOut = true
                    UserDataHolder.uninitialize()
                    finish()
                    return
                }

                pressedOnce = true

                Toast.makeText(
                    this@NavigationActivity,
                    "Нажмите НАЗАД снова, чтобы выйти",
                    Toast.LENGTH_SHORT
                ).show()

                Handler(Looper.myLooper()!!).postDelayed({ pressedOnce = false }, 2000)
            }
        })

        ///////////////////////////

        // Hide/show bottom navigation bar when IME appears/disappears
        window.decorView.setOnApplyWindowInsetsListener { view, insets ->
            val insetsCompat = WindowInsetsCompat.toWindowInsetsCompat(insets, view)
            val imeVisible = insetsCompat.isVisible(WindowInsetsCompat.Type.ime())
            navView.visibility = if (imeVisible) View.GONE else View.VISIBLE
            view.onApplyWindowInsets(insets)
        }
    }

    override fun onPause() {
        super.onPause()
        if (isLoggingOut)
            clearUserPrefs()
        else
            saveUserPrefs()
    }

    private fun saveUserPrefs() {
        Log.i(TAG, "Saving user info...")
        val settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) ?: return
        val userInfoInJson = Gson().toJson(navigationViewModel.userInfo)
        with (settings.edit()) {
            putString("userInfo", userInfoInJson)
            apply()
        }
    }

    private fun clearUserPrefs() {
        Log.i(TAG, "Clearing user info...")
        val settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) ?: return
        settings.edit().clear().apply()
    }

    private fun selectFragment(item: MenuItem) {
        navController.navigate(
            when (item.itemId) {
                selectedItem -> return
                R.id.navigation_notifications -> {
                    R.id.action_global_navigation_notifications
                }
                R.id.navigation_schedule_list -> {
                    when (selectedItem) {
                        // From the notifications fragment
                        R.id.navigation_notifications -> {
                            R.id.action_global_navigation_schedule_list_left
                        }
                        // From the profile fragment
                        R.id.navigation_profile -> {
                            R.id.action_global_navigation_schedule_list_right
                        }
                        else -> R.id.navigation_schedule_list
                    }
                }
                R.id.navigation_profile -> {
                    R.id.action_global_navigation_profile
                }
                else -> item.itemId
            }
        )
    selectedItem = item.itemId
    }
}
