package edu.muiv.univapp.utils

import android.util.Log
import edu.muiv.univapp.api.LoginResponse
import edu.muiv.univapp.ui.login.LoginResult
import java.net.URL
import java.util.*
import java.util.concurrent.Executors

class UserDataHolder private constructor(val user: LoginResult){

    companion object {
        const val BASE_URL = "https://852f-176-195-6-134.eu.ngrok.io/"
        private const val TAG = "UserDataHolder"
        private val URL = URL(BASE_URL)
        private var INSTANCE: UserDataHolder? = null
        private val executor = Executors.newSingleThreadExecutor()

        val isServerOnline: Boolean
            get() {
                return try {
                    executor.execute {
                        URL.openConnection().apply {
                            connectTimeout = 3000
                            connect()
                        }
                        Log.i(TAG, "isServerOnline: true")
                    }
                    true
                } catch (e: Exception) {
                    Log.e(TAG, "Connection check fail", e)
                    false
                }
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
