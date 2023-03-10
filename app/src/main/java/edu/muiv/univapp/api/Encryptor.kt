package edu.muiv.univapp.api

import android.util.Base64
import java.nio.charset.Charset
import java.security.MessageDigest

object Encryptor {
    private const val ENCRYPT_KEY = "NotObviousEncryptKey"
    private val HASH by lazy { sha256() }
    private var hashCharPos = 0
    private var encryptedString = ""

    private fun sha256(): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = ENCRYPT_KEY.toByteArray()
        val digest = md.digest(bytes)
        val hexString = digest.fold("") { str, it -> str + "%02x".format(it) }

        // Convert the string to upper case since "1C" uses upper case too
        return hexString.uppercase()
    }

    private fun encodeString(input: String): String {
        val byteArray = input.toByteArray(Charset.forName("UTF-8"))

        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    /**
     * Returns encrypted string encoded in Base64
     */
    fun encrypt(input: String): String {
        for (char in input) {
            if (hashCharPos >= HASH.length - 1) hashCharPos = 0

            val hashChar = HASH[hashCharPos++]
            val encryptedCharCode = char.code + hashChar.code
            val encryptedChar = encryptedCharCode.toChar()
            encryptedString += encryptedChar
        }

        val encodedString = encodeString(encryptedString)

        // Reset vars
        hashCharPos = 0
        encryptedString = ""

        return encodedString
    }
}
