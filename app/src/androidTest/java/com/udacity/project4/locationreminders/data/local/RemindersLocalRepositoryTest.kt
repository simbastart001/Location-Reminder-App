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
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * @DrStart:     Executes each task synchronously using Architecture Components.
 * */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class RemindersLocalRepositoryTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase
    private lateinit var repository: RemindersLocalRepository

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
         * @DrStart:     Check that the saved and loaded reminders are the same
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
         * @DrStart:     Check that the list is empty
         * */
        assertThat(remindersList, notNullValue())
        remindersList as Result.Success
        assertThat(remindersList.data, `is`(listOf()))
    }

    /**
     * @DrStart:     Attempt to retrieve a non-existing reminder from the database using the repository function
     * */
    @Test
    fun getNonExistingReminder_returnsError() = runBlocking {
        // Choose an ID that is not present in the database
        val nonExistingId = "nonExistingId"

        /**
         * @DrStart:     Attempt to retrieve the non-existing reminder from the database using the repository function
         * */
        val result = repository.getReminder(nonExistingId)

        /**
         * @DrStart:     Check that the result is an error with the expected message
         * */
        assertThat(result, instanceOf(Result.Error::class.java))
        result as Result.Error
        assertThat(result.message, `is`("Reminder not found!"))
    }

    @Test
    fun getReminderById_dataFound() = runBlocking {
//        GIVEN
        val reminder1 = ReminderDTO(
            title = "ISTART HQ",
            description = "Get solutions for your code here",
            location = "Aspindale_Park",
            latitude = -17.872174952286702,
            longitude = 30.954970903694633
        )

//        WHEN
        repository.saveReminder(reminder1)
        val result = repository.getReminder(reminder1.id) as Result.Success

//        THEN
        assertThat(result.data, `is`(notNullValue()))

    }


    @Test
    fun getReminderDataNotFound() = runBlocking {
//        GIVEN
        val reminder1 = ReminderDTO(
            title = "ISTART HQ",
            description = "Get solutions for your code here",
            location = "Aspindale_Park",
            latitude = -17.872174952286702,
            longitude = 30.954970903694633
        )

//        WHEN
        repository.deleteAllReminders()
        val result = repository.getReminder(reminder1.id) as Result.Error

//        THEN
        assertThat(result.message, `is`("Reminder not found!"))

    }


}
