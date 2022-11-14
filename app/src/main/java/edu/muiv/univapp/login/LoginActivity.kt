package edu.muiv.univapp.login

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import edu.muiv.univapp.R

class LoginActivity : AppCompatActivity(), LoginFragmentChoice.Callbacks {

    private companion object {
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container_login)

        if (currentFragment == null) {
            Log.i(TAG, "Creating login fragment...")
            val fragment = LoginFragmentChoice.newInstance()
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container_login, fragment)
                .commit()
        }
    }

    override fun onLoginChoice(isTeacher: Boolean) {
        val fragment = LoginFragment.newInstance(isTeacher)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container_login, fragment)
            .addToBackStack(null)
            .commit()
    }
}
