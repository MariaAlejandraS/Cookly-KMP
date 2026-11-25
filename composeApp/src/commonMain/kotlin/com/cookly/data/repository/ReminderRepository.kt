package com.cookly.data.repository

import com.cookly.data.db.ReminderDao
import com.cookly.data.model.Reminder
import kotlinx.coroutines.flow.Flow

class ReminderRepository(private val dao: ReminderDao) {
    fun observe(ownerId: Long): Flow<List<Reminder>> = dao.observeByOwner(ownerId)
    suspend fun add(reminder: Reminder): Long = dao.insert(reminder)
    suspend fun setEnabled(reminder: Reminder, enabled: Boolean) = dao.update(reminder.copy(enabled = enabled))
    suspend fun delete(reminder: Reminder) = dao.delete(reminder)
}
