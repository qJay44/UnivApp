package edu.muiv.univapp.api

import android.util.Base64
import java.nio.charset.Charset
import java.security.MessageDigest

class Encryptor {
    private val hash by lazy { sha256() }
    private var hashCharPos = 0
    private var encryptedString: String = ""

    companion object {
        private const val ENCRYPT_KEY = "NotObviousEncryptKey"

        private fun encodeString(input: String): String {
            val byteArray = input.toByteArray(Charset.forName("UTF-8"))

            return Base64.encodeToString(byteArray, Base64.DEFAULT)
        }

        private fun sha256(): String {
            val md = MessageDigest.getInstance("SHA-256")
            val bytes = ENCRYPT_KEY.toByteArray()
            val digest = md.digest(bytes)

            return digest.fold("") { str, it -> str + "%02x".format(it) }
        }
    }

    /**
     * Returns [encryptedString] encoded in Base64
     */
    fun encrypt(input: String): String {
        for (char in input) {
            if (hashCharPos >= hash.length - 1) hashCharPos = 0
            hashCharPos++

            val hashChar = hash[hashCharPos]
            val encryptedCharCode = char.code + hashChar.code
            val encryptedChar = encryptedCharCode.toChar()
            encryptedString += encryptedChar
        }

        return encodeString(encryptedString)
    }

    fun reset() {
        hashCharPos = 0
        encryptedString = ""
    }
}
