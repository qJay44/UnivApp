package edu.muiv.univapp.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import edu.muiv.univapp.R

class LoginActivity : AppCompatActivity(), LoginFragmentChoice.Callbacks {

    companion object {
        private const val TAG = ".LoginActivity"

        fun newIntent(context: Context): Intent = Intent(context, LoginActivity::class.java)
    }

    private var lastCallbackParam: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container_login)
        if (currentFragment == null) {
            Log.i(TAG, "Creating login fragment...")
            val fragment = LoginFragmentChoice.newInstance(lastCallbackParam)
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container_login, fragment)
                .commit()
        }
    }

    override fun onLoginChoice(isTeacher: Boolean) {
        lastCallbackParam = isTeacher
        val fragment = LoginFragment.newInstance(isTeacher)
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.slide_out_right
            )
            .replace(R.id.fragment_container_login, fragment)
            .addToBackStack(null)
            .commit()
    }
}
