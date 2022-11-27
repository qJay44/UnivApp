package edu.muiv.univapp.ui.login

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
import edu.muiv.univapp.user.DatabaseTestDataBuilder
import edu.muiv.univapp.user.UserActivity

class LoginFragment : Fragment() {

    companion object {
        private const val TAG = "LoginFragment"
        private const val ADD_TEST_DATA = false

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ADD_TEST_DATA) {

            DatabaseTestDataBuilder.createAll(25)

            for (student in DatabaseTestDataBuilder.studentList)
                loginViewModel.addStudent(student)

            for (teacher in DatabaseTestDataBuilder.teacherList)
                loginViewModel.addTeacher(teacher)

            for (schedule in DatabaseTestDataBuilder.scheduleList)
                loginViewModel.addSchedule(schedule)

            for (notification in DatabaseTestDataBuilder.notificationList)
                loginViewModel.addNotification(notification)

            for (profileAttendance in DatabaseTestDataBuilder.profileAttendanceList)
                loginViewModel.addProfileAttendance(profileAttendance)

            for (subject in DatabaseTestDataBuilder.subjectList)
                loginViewModel.addSubject(subject)
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

        loginViewModel.userLiveData.observe(viewLifecycleOwner) { user ->
            if (user == null) {
                Toast.makeText(requireContext(), "user doesn't exist", Toast.LENGTH_SHORT).show()
            } else {
                loginViewModel.createUserDataHolderInstance(user)
                val intent = Intent(activity, UserActivity::class.java)
                startActivity(intent)
            }
        }

        /////////////////////

        etUsername.addTextChangedListener(loginViewModel.usernameTW)
        etPassword.addTextChangedListener(loginViewModel.passwordTW)

        btnSingIn.setOnClickListener {
            switchVisibility()

            val inputErrorText = loginViewModel.inputValidation()
            Log.i(TAG, "Searching for user...")

            if (inputErrorText == "") {
                val isTeacher = arguments?.getBoolean("isTeacher")!!
                loginViewModel.loadUser(isTeacher)
            } else {
                Toast.makeText(requireContext(), inputErrorText, Toast.LENGTH_SHORT).show()
            }

            switchVisibility()
        }

        // temp
        val u = "stud1"
        val p = "1"
        etUsername.setText(u)
        etPassword.setText(p)
        //
    }

    private fun switchVisibility() {
        progressBar.visibility = visibility

        visibility = if (visibility == View.VISIBLE) View.GONE else View.VISIBLE

        etUsername.visibility = visibility
        etPassword.visibility = visibility
        btnSingIn.visibility = visibility
    }
}