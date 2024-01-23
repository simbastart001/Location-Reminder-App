package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class RemindersDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    /**
     * @DrStart:     Create an in-memory database
     * */
    @Before
    fun setupDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun resetDatabase() = database.close()

    /**
     * @DrStart:     Save a reminder into the database
     * */
    @Test
    fun saveReminder_retrieveReminder_matchingProperties() = runTest {
        val saved = ReminderDTO(
            title = "ISTART HQ",
            description = "Get solutions for your code here",
            location = "Aspindale_Park",
            latitude = -17.872174952286702,
            longitude = 30.954970903694633
        )
        database.reminderDao().saveReminder(saved)

        /**
         * @DrStart:     Retrieve the same reminder from the db
         * */
        val loaded = database.reminderDao().getReminderById(saved.id)

        /**
         * @DrStart:     Check that the saved and loaded reminders are the same
         * */
        assertThat(loaded, notNullValue())
        assertThat(loaded, `is`(saved))
    }

    /**
     * @DrStart:     Save a reminder into the database
     * */
    @Test
    fun saveReminder_clearAll_nullList() = runTest {
        val saved = ReminderDTO(
            title = "ISTART HQ",
            description = "Get solutions for your code here",
            location = "Aspindale_Park",
            latitude = -17.872174952286702,
            longitude = 30.954970903694633
        )
        database.reminderDao().saveReminder(saved)

        /**
         * @DrStart:     Cancel all the reminders from the db and retrieve the result
         * */
        database.reminderDao().deleteAllReminders()
        val remindersList = database.reminderDao().getReminders()

        /**
         * @DrStart:     Check that the list is empty
         * */
        assertThat(remindersList, `is`(listOf()))
    }

}
