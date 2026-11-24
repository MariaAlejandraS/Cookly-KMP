package com.cookly.ui.scan

import androidx.compose.runtime.Composable

/**
 * expect / actual con un Composable: el código compartido pide "un escáner de códigos de barras".
 * - Android (actual): abre la cámara con ZXing y devuelve el código leído.
 * - Desktop (actual): un computador no tiene cámara de escaneo, así que devuelve null
 *   y la pantalla solo muestra la entrada manual del código.
 *
 * Devuelve la función que abre el escáner, o null si la plataforma no tiene.
 */
@Composable
expect fun rememberBarcodeScanner(onScanned: (String) -> Unit): (() -> Unit)?
