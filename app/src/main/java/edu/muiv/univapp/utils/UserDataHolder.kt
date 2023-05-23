package edu.muiv.univapp.utils

import android.util.Log
import edu.muiv.univapp.api.LoginResponse
import edu.muiv.univapp.ui.login.LoginResult
import java.util.UUID

class UserDataHolder private constructor(val user: LoginResult){

    companion object {
        private const val CORE_ADDRESS = "d6f3-46-242-14-212.ngrok-free.app"
        private const val TAG = "UserDataHolder"
        private var INSTANCE: UserDataHolder? = null
        const val BASE_URL = "https://$CORE_ADDRESS"

        val isServerOnline: Boolean
            get() {
                return try {
                    val proc = Runtime.getRuntime().exec("ping -c 1 $CORE_ADDRESS")
                    proc.waitFor()

                    val res = proc.exitValue() == 0
                    Log.i(TAG, "isServerOnline: $res")

                    res
                } catch (e: Exception) {
                    Log.e(TAG, "isServerOnline: false", e)
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
