package com.cookly

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.cookly.data.db.AppDatabase
import com.cookly.data.db.DATABASE_NAME
import java.io.File
import kotlinx.coroutines.Dispatchers

/** En Desktop la base queda en la carpeta del usuario: ~/.cookly/cookly.db (en Windows, C:\Users\<usuario>\.cookly). */
fun createDatabase(): AppDatabase {
    val folder = File(System.getProperty("user.home"), ".cookly").apply { mkdirs() }
    return Room.databaseBuilder<AppDatabase>(
        name = File(folder, DATABASE_NAME).absolutePath,
    )
        .setDriver(BundledSQLiteDriver())
        // Si cambia la versión de la base (se agregó imageUrl), se recrea vacía en vez de fallar.
        .fallbackToDestructiveMigration(dropAllTables = true)
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}
