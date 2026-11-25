package com.cookly.data.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.cookly.data.model.Product
import com.cookly.data.model.Reminder
import com.cookly.data.model.User

/**
 * Base de datos local SQLite con Room (versión multiplataforma).
 * La definición es compartida; solo la creación del archivo cambia por plataforma
 * (ver Database.android.kt y Database.desktop.kt).
 * Versión 2: se agregó la foto del producto (imageUrl).
 */
@Database(entities = [Product::class, Reminder::class, User::class], version = 2)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun reminderDao(): ReminderDao
    abstract fun userDao(): UserDao
}

/** Room genera automáticamente el "actual" de este objeto para cada plataforma (con KSP). */
@Suppress("NO_ACTUAL_FOR_EXPECT", "KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

const val DATABASE_NAME = "cookly.db"
