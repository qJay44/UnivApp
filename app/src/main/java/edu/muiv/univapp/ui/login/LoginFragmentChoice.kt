package edu.muiv.univapp.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import edu.muiv.univapp.R
import edu.muiv.univapp.api.LoginResponse
import edu.muiv.univapp.ui.navigation.NavigationActivity
import edu.muiv.univapp.utils.UserDataHolder

class LoginFragmentChoice : Fragment() {

    companion object {
        private const val TAG = "LoginFragmentChoice"
        private const val PREFS_NAME = "USER_INFO"

        fun newInstance(isTeacher: Boolean?): LoginFragmentChoice {
            return if (isTeacher != null) {
                val args = Bundle().apply {
                    putBoolean("isTeacher", isTeacher)
                }
                LoginFragmentChoice().apply {
                    arguments = args
                }
            } else {
                LoginFragmentChoice()
            }
        }
    }

    interface Callbacks {
        fun onLoginChoice(isTeacher: Boolean)
    }

    private lateinit var pbLoading: ProgressBar
    private lateinit var clView: ConstraintLayout
    private lateinit var btnAsStudent: Button
    private lateinit var btnAsTeacher: Button

    private var callbacks: Callbacks? = null
    private var lastCallbackParam: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lastCallbackParam = arguments?.getBoolean("isTeacher")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login_choice, container, false)

        pbLoading = view.findViewById(R.id.pbLoading)
        clView = view.findViewById(R.id.constraintLayout)
        btnAsStudent = view.findViewById(R.id.btnAsStudent)
        btnAsTeacher = view.findViewById(R.id.btnAsTeacher)

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pbLoading.visibility = View.VISIBLE
        clView.visibility = View.GONE

        btnAsStudent.setOnClickListener {
            callbacks?.onLoginChoice(false)
        }

        btnAsTeacher.setOnClickListener {
            callbacks?.onLoginChoice(true)
        }
    }

    override fun onResume() {
        super.onResume()
        if (loadUserPrefs() && UserDataHolder.isServerOnline) {
            Log.i(TAG, "Authorising the user...")
            val intent = Intent(requireActivity(), NavigationActivity::class.java)
            startActivity(intent)
        } else {
            pbLoading.visibility = View.GONE
            clView.visibility = View.VISIBLE
            lastCallbackParam?.let { callbacks?.onLoginChoice(it) }
        }
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }

    private fun loadUserPrefs(): Boolean {
        val settings = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) ?: return false
        val userInfoAsString = settings.getString("userInfo", null)
        val userInfo = Gson().fromJson(userInfoAsString, LoginResponse::class.java) ?: return false
        UserDataHolder.initialize(userInfo)

        return true
    }
}
