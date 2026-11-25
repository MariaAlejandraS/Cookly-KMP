package com.cookly

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.cookly.data.db.AppDatabase
import com.cookly.data.db.DATABASE_NAME
import kotlinx.coroutines.Dispatchers

/** En Android la base queda en la carpeta privada de la app (/data/data/com.cookly/databases). */
fun createDatabase(context: Context): AppDatabase {
    val file = context.applicationContext.getDatabasePath(DATABASE_NAME)
    return Room.databaseBuilder<AppDatabase>(
        context = context.applicationContext,
        name = file.absolutePath,
    )
        .setDriver(BundledSQLiteDriver())
        // Si cambia la versión de la base (se agregó imageUrl), se recrea vacía en vez de fallar.
        .fallbackToDestructiveMigration(dropAllTables = true)
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}
