package edu.muiv.univapp.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import edu.muiv.univapp.R
import edu.muiv.univapp.ui.login.utils.DatabaseTestDataBuilder
import edu.muiv.univapp.ui.navigation.NavigationActivity

class LoginFragment : Fragment() {

    companion object {
        private const val TAG = "LoginFragment"
        private const val ADD_TEST_DATA = false
        private const val PREFS_STUDENT = "STUDENT_TYPE"
        private const val PREFS_TEACHER = "TEACHER_TYPE"
        private const val PREF_USERNAME = "USERNAME_PREF"
        private const val PREF_PASSWORD = "PASSWORD_PREF"
        private const val PREF_USER_TYPE = "USER_TYPE_PREF"

        fun newInstance(isTeacher: Boolean): LoginFragment {
            val args = Bundle().apply {
                putBoolean("isTeacher", isTeacher)
            }
            return LoginFragment().apply {
                arguments = args
            }
        }
    }

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnSingIn: Button
    private lateinit var progressBar: ProgressBar
    private var visibility = View.VISIBLE

    private val loginViewModel: LoginViewModel by lazy {
        ViewModelProvider(this)[LoginViewModel::class.java]
    }

    private val isTeacher by lazy { requireArguments().getBoolean("isTeacher") }
    private val settings by lazy {
        if (isTeacher)
            activity?.getSharedPreferences(PREFS_TEACHER, Context.MODE_PRIVATE)
        else
            activity?.getSharedPreferences(PREFS_STUDENT, Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ADD_TEST_DATA) {

            with (DatabaseTestDataBuilder) {
                createAll(70)

                for (student in studentList)
                    loginViewModel.addStudent(student)

                for (teacher in teacherList)
                    loginViewModel.addTeacher(teacher)

                for (subject1 in subject1List)
                    loginViewModel.addSubject(subject1)

                for (subject2 in subject2List)
                    loginViewModel.addSubject(subject2)

                for (schedule in scheduleList)
                    loginViewModel.addSchedule(schedule)

                for (notification in notificationList)
                    loginViewModel.addNotification(notification)

                for (profileAttendance in profileAttendanceList)
                    loginViewModel.addProfileAttendance(profileAttendance)

                Log.w(TAG, "Created new test data:\n" +
                    "Students: ${studentList.size}\n" +
                    "Teachers: ${teacherList.size}\n" +
                    "Subjects: ${subject1List.size + subject2List.size}\n" +
                    "Schedules: ${scheduleList.size}\n" +
                    "Notifications: ${notificationList.size}\n" +
                    "ProfileAttendances: ${profileAttendanceList.size}\n"
                )
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        etUsername = view.findViewById(R.id.etUsername)
        etPassword = view.findViewById(R.id.etPassword)
        btnSingIn = view.findViewById(R.id.btnLogin)
        progressBar = view.findViewById(R.id.pbLoading)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Sign-in process //

        loginViewModel.responseCode.observe(viewLifecycleOwner) { statusCode ->
            when (statusCode) {
                // Wrong credentials
                204 -> {
                    val msg = "Логин или пароль введены неправильно"
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                }
                // Sign-in user
                200 -> {
                    val intent = Intent(activity, NavigationActivity::class.java)
                    startActivity(intent)
                }
                // Server failure response
                500 -> {
                    val msg = "Произошла неизвестная ошибка, попробуйте снова позже"
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                }
            }
        }

        /////////////////////

        etUsername.addTextChangedListener(loginViewModel.usernameTW)
        etPassword.addTextChangedListener(loginViewModel.passwordTW)

        btnSingIn.setOnClickListener {
            switchVisibility()

            val inputErrorText = loginViewModel.inputValidation()
            Log.i(TAG, "Searching for user...")

            if (inputErrorText == null) {
                val isTeacher = arguments?.getBoolean("isTeacher")!!
                loginViewModel.loadUser(isTeacher)
            } else {
                Toast.makeText(requireContext(), inputErrorText, Toast.LENGTH_SHORT).show()
            }

            switchVisibility()
        }
    }

    override fun onResume() {
        super.onResume()
        loadPreferences()
    }

    override fun onPause() {
        super.onPause()
        savePreferences()
    }

    private fun loadPreferences() {
        if (settings == null) return
        with (settings!!) {
            val username = getString(PREF_USERNAME, null)
            val password = getString(PREF_PASSWORD, null)

            etUsername.setText(username)
            etPassword.setText(password)
        }
    }

    private fun savePreferences() {
        if (settings == null) return
        with (settings!!.edit()) {
            putString(PREF_USERNAME, etUsername.text.toString())
            putString(PREF_PASSWORD, etPassword.text.toString())
            putBoolean(PREF_USER_TYPE, isTeacher)
            apply()
        }
    }

    private fun switchVisibility() {
        progressBar.visibility = visibility

        visibility = if (visibility == View.VISIBLE) View.GONE else View.VISIBLE

        etUsername.visibility = visibility
        etPassword.visibility = visibility
        btnSingIn.visibility = visibility
    }
}
