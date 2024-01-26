package com.udacity.project4.locationreminders.data.local

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

class FakeDataSource(private var reminders: MutableList<ReminderDTO> = mutableListOf()) :
    ReminderDataSource {

    private var shouldReturnError = false
    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) {
            return Result.Error("Reminders data not found")
        }
        return Result.Success(reminders)
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders.add(reminder)
    }

    override suspend fun deleteReminder(id: String): Result<Unit> {
        val reminder = reminders.find { it.id == id }

        return if (reminder == null) {
            Result.Error("Reminder data not found!")
        } else {
            reminders.remove(reminder)
            Result.Success(Unit)
        }
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (shouldReturnError) {
            return Result.Error("Reminders data not found")
        }

        val reminder = reminders.find { it.id == id }
        return if (reminder == null) {
            Result.Error("Reminder data not found!")
        } else {
            Result.Success(reminder)
        }
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
    }
}
