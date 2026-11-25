package com.cookly.data.auth

import com.cookly.secureRandomBytes
import com.cookly.sha256

/**
 * La contraseña nunca se guarda. Se guarda una huella (hash):
 * SHA-256 de (sal + contraseña), repetido 50.000 veces.
 * sha256() y secureRandomBytes() son "expect": cada plataforma da su "actual".
 */
object PasswordHasher {
    const val ITERATIONS = 50_000

    fun newSalt(): String = secureRandomBytes(16).toHex()

    fun hash(password: String, salt: String): String {
        var data = (salt + password).encodeToByteArray()
        repeat(ITERATIONS) { data = sha256(data) }
        return data.toHex()
    }

    /** Compara siempre todos los caracteres, para que el tiempo no delate cuánto coincidió. */
    fun matches(password: String, salt: String, expectedHash: String): Boolean {
        val actual = hash(password, salt)
        if (actual.length != expectedHash.length) return false
        var diff = 0
        for (i in actual.indices) diff = diff or (actual[i].code xor expectedHash[i].code)
        return diff == 0
    }

    private fun ByteArray.toHex(): String =
        joinToString("") { (it.toInt() and 0xFF).toString(16).padStart(2, '0') }
}
