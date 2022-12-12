package edu.muiv.univapp.utils

import android.util.Log
import edu.muiv.univapp.ui.login.LoginResult

class UserDataHolder private constructor(val user: LoginResult){

    companion object {
        private const val TAG = "UserDataHolder"
        private var INSTANCE: UserDataHolder? = null

        fun initialize(user: LoginResult) {
            if (INSTANCE == null) {
                INSTANCE = UserDataHolder(user)
                Log.i(TAG, user.toString())
            } else {
                Log.w(TAG, "UserDataHolder already initialized")
            }
        }

        fun get(): UserDataHolder {
            return INSTANCE ?: throw IllegalStateException("UserDataHolder must be initialized")
        }

        fun uninitialize() {
            INSTANCE = null
            Log.i(TAG, "UserDataHolder has been uninitialized")
        }
    }
}
