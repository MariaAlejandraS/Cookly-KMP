package com.cookly.ui.scan

import androidx.compose.runtime.Composable

/** En Desktop no hay escáner con cámara: la pantalla usa solo la entrada manual del código. */
@Composable
actual fun rememberBarcodeScanner(onScanned: (String) -> Unit): (() -> Unit)? = null
