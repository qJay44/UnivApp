package edu.muiv.univapp.utils

import android.util.Log
import edu.muiv.univapp.api.LoginResponse
import edu.muiv.univapp.ui.login.LoginResult
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class UserDataHolder private constructor(val user: LoginResult){

    companion object {
        private const val TAG = "UserDataHolder"
        private var INSTANCE: UserDataHolder? = null
        const val BASE_URL = "http://localhost:3000"

        val isServerOnline: Boolean
            get() {
                val arr = BooleanArray(1)

                Thread {
                    arr[0] =
                        try {
                            val url = URL(BASE_URL)
                            val urlc = url.openConnection() as HttpURLConnection

                            urlc.connectTimeout = 5 * 1000
                            urlc.connect()
                            urlc.responseCode == 200
                        } catch (e: Exception) {
                            false
                        }
                }.apply {
                    start()
                    join()
                }

                Log.i(TAG, "isServerOnline: ${arr[0]}")
                return arr[0]
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
