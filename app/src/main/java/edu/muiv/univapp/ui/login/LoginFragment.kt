package edu.muiv.univapp.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import edu.muiv.univapp.R
import edu.muiv.univapp.api.LoginResponse
import edu.muiv.univapp.ui.navigation.NavigationActivity
import edu.muiv.univapp.utils.UserDataHolder

class LoginFragment : Fragment() {

    companion object {
        private const val TAG = "LoginFragment"
        private const val ADD_TEST_DATA = false
        private const val PREFS_STUDENT = "STUDENT_TYPE"
        private const val PREFS_TEACHER = "TEACHER_TYPE"
        private const val PREF_USERNAME = "USERNAME_PREF"
        private const val PREF_PASSWORD = "PASSWORD_PREF"
        private const val PREF_USER_TYPE = "USER_TYPE_PREF"
        private const val PREFS_NAME = "USER_INFO"

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
    private lateinit var btnSignInOffline: Button

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
        if (ADD_TEST_DATA) loginViewModel.addAll(70, TAG)
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
        btnSignInOffline = view.findViewById(R.id.btnLoginOffline)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etUsername.addTextChangedListener(loginViewModel.usernameTW)
        etPassword.addTextChangedListener(loginViewModel.passwordTW)

        // Animations //

        // Progress bar appearing
        val pbAnimationFadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        pbAnimationFadeIn.setAnimationListener(ProgressBarAnimationListener(false))

        // Progress bar disappearing
        val pbAnimationFadeOut = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_out)
        pbAnimationFadeOut.setAnimationListener(ProgressBarAnimationListener(true))

        // Button appearing
        val btnAnimationFadeOut = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_out)
        btnAnimationFadeOut.setAnimationListener(ButtonAnimationListener(true, pbAnimationFadeIn))

        // Button disappearing
        val btnAnimationFadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        btnAnimationFadeIn.setAnimationListener(ButtonAnimationListener(false, pbAnimationFadeOut))

        ////////////////

        btnSingIn.setOnClickListener { btn ->
            btn.isEnabled = false
            btn.startAnimation(btnAnimationFadeOut)

            val inputErrorText = loginViewModel.inputValidation()
            Log.i(TAG, "Searching for user...")

            if (inputErrorText == null) {
                val isTeacher = arguments?.getBoolean("isTeacher")!!
                loginViewModel.loadUser(isTeacher)
            } else {
                showToast(inputErrorText)
            }
        }

        btnSignInOffline.setOnClickListener {
            if (loadOfflineUser()) {
                val intent = Intent(activity, NavigationActivity::class.java)
                startActivity(intent)
            } else {
                showToast("Данные пользователя не были найдены")
            }
        }

        // Sign-in process //

        loginViewModel.responseCode.observe(viewLifecycleOwner) { statusCode ->
            when (statusCode) {
                /**
                 * [statusCode] ->
                 * 204: Invalid credentials
                 * 200: Valid credentials, sign-in user
                 * 500: Server failure response
                 * 503: Service is unavailable
                 */

                204 -> showToast("Логин или пароль введены неправильно")
                200 -> startActivity(Intent(activity, NavigationActivity::class.java))
                500 -> showToast("Произошла неизвестная ошибка, попробуйте позже")
                503 -> showToast("Сервер не доступен, попробуйте позже")
            }
            btnSingIn.startAnimation(btnAnimationFadeIn)
            btnSingIn.isEnabled = true
        }

        /////////////////////
    }

    override fun onResume() {
        super.onResume()
        loadPreferences()
    }

    override fun onPause() {
        super.onPause()
        savePreferences()
    }

    private fun loadOfflineUser(): Boolean {
        val settings = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) ?: return false
        val userInfoAsString = settings.getString("userInfo", null)
        val userInfo = Gson().fromJson(userInfoAsString, LoginResponse::class.java) ?: return false
        UserDataHolder.initialize(userInfo)

        return true
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

    private fun showToast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    // Animation for progress bar
    inner class ProgressBarAnimationListener(private val isFading: Boolean) : AnimationListener {
        override fun onAnimationStart(p0: Animation?) {
            if (!isFading) {
                progressBar.alpha = 0f
                progressBar.visibility = View.VISIBLE
            }
        }

        override fun onAnimationEnd(p0: Animation?) {
            if (isFading)
                progressBar.visibility = View.GONE
            else
                progressBar.alpha = 1f
        }

        override fun onAnimationRepeat(p0: Animation?) {}
    }

    // Animation for sign-in button
    inner class ButtonAnimationListener(
        private val isFading: Boolean,
        private val pbAnimation: Animation
        ) : AnimationListener {

        override fun onAnimationStart(p0: Animation?) {
            if (isFading) {
                // Progress bar fade out
                progressBar.startAnimation(pbAnimation)
            } else {
                btnSingIn.alpha = 0f
                btnSingIn.visibility = View.VISIBLE
                // Progress bar fade in
                progressBar.startAnimation(pbAnimation)
            }
        }

        override fun onAnimationEnd(p0: Animation?) {
            if (isFading) {
                btnSingIn.visibility = View.INVISIBLE
            } else {
                btnSingIn.alpha = 1f
            }
        }

        override fun onAnimationRepeat(p0: Animation?) {}
    }
}
