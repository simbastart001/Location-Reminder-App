package com.udacity.project4.utils

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Room
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.RemindersDao
import com.udacity.project4.locationreminders.data.local.RemindersDatabase
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository

/**
 * A Service Locator for the [ReminderDataSource]. This is the prod version, with a
 * the real [RemindersLocalRepository].
 */
object ServiceLocator {

    /**
     * @DrStart:     Volatile means that writes to this field are immediately made visible to other threads.
     * */
    @Volatile
    var repository: ReminderDataSource? = null
        @VisibleForTesting set

    var database: RemindersDatabase? = null

    fun getRepository(context: Context): ReminderDataSource {
        synchronized(this) {
            return repository ?: createRepository(context)
        }
    }

    /**
     * @DrStart:     Create a new repository. This is called by getRepository.
     * */
    private fun createRepository(context: Context): ReminderDataSource {
        val newRepo = RemindersLocalRepository(createRemindersDao(context))
        repository = newRepo
        return newRepo
    }

    fun createRemindersDao(context: Context): RemindersDao {
        val database = database ?: createDatabase(context)
        return database.reminderDao()
    }

    private fun createDatabase(context: Context): RemindersDatabase {
        val db = Room.databaseBuilder(
            context.applicationContext,
            RemindersDatabase::class.java, "locationReminders.db"
        ).build()
        database = db

        return db
    }

    @VisibleForTesting
    fun resetRepository() {
        synchronized(lock = Any()) {
            database?.apply {
                clearAllTables()
                close()
            }
            database = null
            repository = null
        }
    }

}
