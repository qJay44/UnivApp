package edu.muiv.univapp.login

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
import edu.muiv.univapp.schedule.ScheduleActivity
import edu.muiv.univapp.user.DatabaseTestDataBuilder

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

            DatabaseTestDataBuilder.createAll(12)

            for (student in DatabaseTestDataBuilder.studentList)
                loginViewModel.addStudent(student)

            for (teacher in DatabaseTestDataBuilder.teacherList)
                loginViewModel.addTeacher(teacher)
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

        loginViewModel.userLiveData.observe(viewLifecycleOwner) { user ->
            if (user == null) {
                Toast.makeText(requireContext(), "user doesn't exist", Toast.LENGTH_SHORT).show()
            } else {
                Log.i(TAG, "Starting new activity...")
                val intent = Intent(activity, ScheduleActivity::class.java)
                val bundle = loginViewModel.getUserBundle(user)
                intent.putExtra("userBundle", bundle)
                startActivity(intent)
            }
        }

        etUsername.addTextChangedListener(loginViewModel.usernameTW)
        etPassword.addTextChangedListener(loginViewModel.passwordTW)

        btnSingIn.setOnClickListener {
            switchVisibility()

            val inputErrorText = loginViewModel.inputValidation()

            try {
                if (inputErrorText == "") {
                    val isTeacher = arguments?.getBoolean("isTeacher")!!
                    loginViewModel.loadUser(isTeacher)
                } else {
                    Toast.makeText(requireContext(), inputErrorText, Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            } finally {
                switchVisibility()
            }
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