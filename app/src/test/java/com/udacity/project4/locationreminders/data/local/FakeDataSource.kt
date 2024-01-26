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
            // Simulate an exception being thrown as it would in the real local data source
            return Result.Error("TestException")
        }
        return Result.Success(reminders)
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        if (shouldReturnError) {
            throw Exception("TestException")
        }
        reminders.add(reminder)
    }

    override suspend fun deleteReminder(id: String): Result<Unit> {
        if (shouldReturnError) {
            return Result.Error("TestException")
        }
        val reminder = reminders.find { it.id == id }

        return if (reminder == null) {
            Result.Error("Reminder not found!")
        } else {
            reminders.remove(reminder)
            Result.Success(Unit)
        }
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (shouldReturnError) {
            return Result.Error("TestException")
        }

        val reminder = reminders.find { it.id == id }
        return if (reminder == null) {
            Result.Error("Reminder not found!")
        } else {
            Result.Success(reminder)
        }
    }

    override suspend fun deleteAllReminders() {
        if (shouldReturnError) {
            throw Exception("TestException")
        }
        reminders.clear()
    }

}
