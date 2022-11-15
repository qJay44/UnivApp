package edu.muiv.univapp.user

import edu.muiv.univapp.ui.login.LoginResult

class UserDataHolder private constructor(val user: LoginResult){

    companion object {
        private var INSTANCE: UserDataHolder? = null

        fun initialize(user: LoginResult) {
            if (INSTANCE == null) {
                INSTANCE = UserDataHolder(user)
            }
        }

        fun get(): UserDataHolder {
            return INSTANCE ?: throw IllegalStateException("UserDataHolder must be initialized")
        }
    }
}
