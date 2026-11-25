package com.cookly.data

import com.cookly.data.db.AppDatabase
import com.cookly.data.network.FoodApi
import com.cookly.data.prefs.UserPrefs
import com.cookly.data.repository.AuthRepository
import com.cookly.data.repository.ProductRepository
import com.cookly.data.repository.ReminderRepository

/**
 * Contenedor de dependencias: crea una sola vez la base, la red y los repositorios.
 * Cada plataforma crea la base a su manera y se la pasa aquí; desde este punto todo es compartido.
 */
class AppContainer(database: AppDatabase) {
    val prefs = UserPrefs()
    val auth = AuthRepository(database.userDao(), prefs)
    val products = ProductRepository(database.productDao())
    val reminders = ReminderRepository(database.reminderDao())
    val foodApi = FoodApi()
}
