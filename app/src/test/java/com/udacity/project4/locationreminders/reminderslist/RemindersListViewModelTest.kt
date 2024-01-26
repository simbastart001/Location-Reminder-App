package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.local.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

/**
 * @DrStart:    This is a test class for the [RemindersListViewModel] class.
 *             It is used to test the implementation of [RemindersListViewModel] in isolation
 *             using a fake repository and a fake data source.
 * */
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var dataSource: FakeDataSource
    private lateinit var application: Application

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setupViewModel() {
        stopKoin()
        application = ApplicationProvider.getApplicationContext()
        FirebaseApp.initializeApp(application)
        dataSource = FakeDataSource()
        remindersListViewModel = RemindersListViewModel(application, dataSource)
    }

    /**
     * @DrStart:    check_loading()
     * */
    @Test
    fun check_loading() = runTest(UnconfinedTestDispatcher()) {
        // GIVEN
        val reminder = ReminderDTO(
            title = "ISTART HQ",
            description = "Get solutions for your code here",
            location = "Aspindale_Park",
            latitude = -17.872174952286702,
            longitude = 30.954970903694633
        )
        Dispatchers.setMain(StandardTestDispatcher())

        // WHEN
        dataSource.saveReminder(reminder)
        remindersListViewModel.loadReminders()

        // THEN
        assertThat(
            remindersListViewModel.showLoading.getOrAwaitValue(), `is`(true)
        )

        advanceUntilIdle()
        assertThat(
            remindersListViewModel.showLoading.getOrAwaitValue(), `is`(false)
        )

    }

    @Test
    fun shouldReturnError() = runTest(UnconfinedTestDispatcher()) {
        // GIVEN
        dataSource.setReturnError(true)

        // WHEN
        remindersListViewModel.loadReminders()

        // THEN
        assertThat(
            remindersListViewModel.showNoData.getOrAwaitValue(), `is`(true)
        )
        assertThat(
            remindersListViewModel.showSnackBar.getOrAwaitValue(), `is`("Reminders data not found")
        )

        // Reset the shouldReturnError flag after the test
        dataSource.setReturnError(false)

    }

}
