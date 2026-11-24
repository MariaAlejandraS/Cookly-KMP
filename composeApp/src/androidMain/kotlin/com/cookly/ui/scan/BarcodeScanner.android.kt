package com.cookly.ui.scan

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

/** En Android: ZXing abre la cámara (y pide el permiso), lee el código y lo devuelve. */
@Composable
actual fun rememberBarcodeScanner(onScanned: (String) -> Unit): (() -> Unit)? {
    val callback = rememberUpdatedState(onScanned)
    val launcher = rememberLauncherForActivityResult(ScanContract()) { result ->
        result.contents?.takeIf { it.isNotBlank() }?.let { callback.value(it) }
    }
    return remember(launcher) {
        {
            val options = ScanOptions()
                .setDesiredBarcodeFormats(ScanOptions.PRODUCT_CODE_TYPES) // EAN-13, EAN-8, UPC
                .setPrompt("Apunta al código de barras")
                .setBeepEnabled(true)
                .setOrientationLocked(false)
            launcher.launch(options)
        }
    }
}
