package edu.muiv.univapp.utils

import android.util.Log
import edu.muiv.univapp.api.LoginResponse
import edu.muiv.univapp.ui.login.LoginResult
import java.util.*

class UserDataHolder private constructor(val user: LoginResult){

    companion object {
        private const val TAG = "UserDataHolder"
        private var INSTANCE: UserDataHolder? = null
        val isOnline: Boolean
            get() = run {
                val command = "ping -c 1 localhost:3000"
                val latency = Runtime.getRuntime().exec(command).waitFor()
                Log.i(TAG, "Server latency: $latency ms")

                latency <= 100
            }

        fun initialize(loginResponse: LoginResponse) {
            if (INSTANCE == null) {
                val user =
                    with(loginResponse) {
                        /** Just in case:
                         * Left side - [LoginResult], right side - [LoginResponse] */

                        LoginResult(
                            id = UUID.fromString(id),
                            name = name,
                            surname = surname,
                            patronymic = patronymic,
                            groupName = groupName,
                            course = course,
                            semester = semester,
                        )
                    }
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
