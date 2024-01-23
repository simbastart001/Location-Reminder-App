package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class RemindersLocalRepositoryTest {

    /**
     * @DrStart:     Executes each task synchronously using Architecture Components.
     * */
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase
    private lateinit var repository: RemindersLocalRepository

    /**
     * @DrStart:     Create an in-memory database and initialize the repository
     * */
    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        repository = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    @After
    fun reset() = database.close()

    /**
     * @DrStart:     Save a reminder into the database using the repository function
     * */
    @Test
    fun saveReminder_retrieveReminder_machingProperties() = runBlocking {
        val saved = ReminderDTO(
            title = "ISTART HQ",
            description = "Get solutions for your code here",
            location = "Aspindale_Park",
            latitude = -17.872174952286702,
            longitude = 30.954970903694633
        )
        repository.saveReminder(saved)

        /**
         * @DrStart:     Retrieve the reminder from the database using the repository function
         * */
        val loaded = repository.getReminder(saved.id)

        /**
         * @DrStart:     Then check that the retrieved reminder matches the reminder that was saved
         * */
        assertThat(loaded, notNullValue())
        loaded as Result.Success
        assertThat(loaded.data.title, `is`(saved.title))
        assertThat(loaded.data.description, `is`(saved.description))
        assertThat(loaded.data.location, `is`(saved.location))
        assertThat(loaded.data.latitude, `is`(saved.latitude))
        assertThat(loaded.data.longitude, `is`(saved.longitude))
    }

    /**
     * @DrStart:     Save a reminder into the database using the repository function
     * */
    @Test
    fun saveReminder_clearAll_nullList() = runBlocking {
        // GIVEN: save a reminder into the database using the repository function
        val saved = ReminderDTO(
            title = "ISTART HQ",
            description = "Get solutions for your code here",
            location = "Aspindale_Park",
            latitude = -17.872174952286702,
            longitude = 30.954970903694633
        )
        repository.saveReminder(saved)

        /**
         * @DrStart:     Clear all reminders from the database using the repository function
         * */

        repository.deleteAllReminders()
        val remindersList = repository.getReminders()

        /**
         * @DrStart:     Then check that the retrieved reminder matches the reminder that was saved
         * */
        assertThat(remindersList, notNullValue())
        remindersList as Result.Success
        assertThat(remindersList.data, `is`(listOf()))
    }
}