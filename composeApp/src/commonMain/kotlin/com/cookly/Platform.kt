package com.cookly

// expect / actual: el código compartido DECLARA lo que necesita ("expect")
// y cada plataforma lo IMPLEMENTA a su manera ("actual") en androidMain y desktopMain.

/** Nombre de la plataforma donde corre la app (se muestra en Perfil y Acerca de). */
expect fun platformName(): String

/** Calcula el SHA-256 de unos bytes (se usa para la huella de las contraseñas). */
expect fun sha256(bytes: ByteArray): ByteArray

/** Bytes aleatorios seguros (para la sal de cada contraseña). */
expect fun secureRandomBytes(size: Int): ByteArray
