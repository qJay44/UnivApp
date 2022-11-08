package edu.muiv.univapp.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import edu.muiv.univapp.R
import edu.muiv.univapp.schedule.ScheduleActivity
import edu.muiv.univapp.user.User
import edu.muiv.univapp.user.UserTestDataBuilder
import edu.muiv.univapp.user.UserViewModel

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val ADD_TEST_DATA = false
    }

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnSingIn: Button
    private lateinit var progressBar: ProgressBar

    private val login = Login()
    private val loginTW = LoginTextWatcher(login)
    private var visibility = View.VISIBLE

    private val userViewModel: UserViewModel by lazy {
        ViewModelProvider(this)[UserViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if (ADD_TEST_DATA) {
            val users = UserTestDataBuilder.createUser(5)
            for (user in users) userViewModel.addUser(user)
        }

        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnSingIn = findViewById(R.id.btnLogin)
        progressBar = findViewById(R.id.pbLoading)

        etUsername.addTextChangedListener(loginTW.usernameTW)
        etPassword.addTextChangedListener(loginTW.passwordTW)

        userViewModel.userLiveData.observe(this) { user ->
            if (user == null) {
                Toast.makeText(this, "user doesn't exist", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this@LoginActivity, ScheduleActivity::class.java)
                val bundle = packUserBundle(user)
                intent.putExtra("userBundle", bundle)
                startActivity(intent)
            }
            switchVisibility()
        }

        btnSingIn.setOnClickListener {
            val inputErrorText =
                when ("") {
                    login.username -> "Login field can't be empty"
                    login.password -> "Password field can't be empty"
                    else -> ""
                }

            if (inputErrorText != "") {
                Toast.makeText(this, inputErrorText, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            switchVisibility()
            userViewModel.loadUser(login)
        }
    }

    private fun switchVisibility() {
        progressBar.visibility = visibility

        visibility = if (visibility == View.VISIBLE) View.GONE else View.VISIBLE

        etUsername.visibility = visibility
        etPassword.visibility = visibility
        btnSingIn.visibility = visibility
    }

    private fun packUserBundle(user: User): Bundle {
        val bundle = Bundle().apply {
            putSerializable("id", user.id)
            putString("login", user.login)
            putString("password", user.password)
            putString("name", user.name)
            putString("surname", user.surname)
            putString("userGroup", user.userGroup)
            putString("studentGroup", user.studentGroup)
        }

        return bundle
    }
}
