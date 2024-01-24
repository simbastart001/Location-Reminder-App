package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.local.FakeDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

/**
 * @DrStart:    Test the implementation of [SaveReminderViewModel] in isolation
 *              using a fake repository and a fake data source.
 * */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var application: Application
    private lateinit var dataSource: FakeDataSource
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    @Before
    fun setupViewModel() {
        stopKoin()
        application = ApplicationProvider.getApplicationContext()
        FirebaseApp.initializeApp(application)
        dataSource = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(application, dataSource)
    }

    @Test
    fun validate_loadingOfReminders() = runTest {
        //GIVEN
        val reminder = ReminderDataItem(
            "ISTART HQ", "Get solutions for your code here",
            "Aspindale_Park", -17.872174952286702, 30.954970903694633
        )

        //WHEN
        mainCoroutineRule.pauseDispatcher()
        saveReminderViewModel.validateAndSaveReminder(reminder)

        //THEN
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(true))

        //WHEN
        mainCoroutineRule.resumeDispatcher()

        //THEN
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun validate_savingRemindersLocationEmpty() = runTest {
        //GIVEN
        val reminder = ReminderDataItem(
            "ISTART HQ", "Get solutions for your code here",
            "Aspindale_Park", -17.872174952286702, 30.954970903694633
        )

        //WHEN
        saveReminderViewModel.validateAndSaveReminder(reminder)

        //THEN
        assertThat(
            saveReminderViewModel.showToast.getOrAwaitValue(),
            `is`(saveReminderViewModel.app.getString(R.string.reminder_saved))
        )
        assertThat(
            saveReminderViewModel.navigationCommand.getOrAwaitValue(),
            `is`(NavigationCommand.Back)
        )
    }

    @Test
    fun validate_savingRemindersTitleNull() = runTest {
        //GIVEN
        val reminder = ReminderDataItem(
            "ISTART HQ", "Get solutions for your code here",
            "Aspindale_Park", -17.872174952286702, 30.954970903694633
        )

        //WHEN
        mainCoroutineRule.pauseDispatcher()
        saveReminderViewModel.validateAndSaveReminder(reminder)

        //THEN
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(true))

    }

}
