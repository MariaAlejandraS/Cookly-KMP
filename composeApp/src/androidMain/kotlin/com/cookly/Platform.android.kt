package com.cookly

import android.os.Build
import java.security.MessageDigest
import java.security.SecureRandom

actual fun platformName(): String = "Android ${Build.VERSION.RELEASE}"

actual fun sha256(bytes: ByteArray): ByteArray = MessageDigest.getInstance("SHA-256").digest(bytes)

actual fun secureRandomBytes(size: Int): ByteArray = ByteArray(size).also { SecureRandom().nextBytes(it) }
