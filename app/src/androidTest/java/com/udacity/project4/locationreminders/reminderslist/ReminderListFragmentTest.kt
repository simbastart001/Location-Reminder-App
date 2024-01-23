package com.udacity.project4.locationreminders.reminderslist

import android.content.Context
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.local.FakeAndroidDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.ServiceLocator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

/**
 * @DrStart:     This test class is used to test the implementation of the fragments.
 * */
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {

    private lateinit var appContext: Context
    private lateinit var repository: FakeAndroidDataSource

    /**
     * @DrStart:     This function is used to setup the test environment using Koin.
     * */
    @Before
    fun setup() {
        stopKoin()

        appContext = ApplicationProvider.getApplicationContext()
        repository = FakeAndroidDataSource()
        ServiceLocator.repository = repository

        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    get(),
                    get() as ReminderDataSource
                )
            }
            //Declare singleton definitions to be later injected using by inject()
            single {
                SaveReminderViewModel(
                    get(),
                    get() as ReminderDataSource
                )
            }
            single { ServiceLocator.getRepository(appContext) }
        }

        startKoin {
            androidContext(appContext)
            modules(listOf(myModule))
        }
    }

    @After
    fun reset() {
        ServiceLocator.resetRepository()
    }

    /**
     * @DrStart:     This test is used to check that the ReminderListFragment is displayed correctly.
     * */
    @Test
    fun reminderListFragment_reminderList_checkContent() = runBlockingTest {
        val reminder = ReminderDTO(
            title = "ISTART HQ",
            description = "Get solutions for your code here",
            location = "Aspindale_Park",
            latitude = -17.872174952286702,
            longitude = 30.954970903694633
        )
        repository.saveReminder(reminder)

        launchFragmentInContainer<ReminderListFragment>(themeResId = R.style.AppTheme)

        onView(withId(R.id.reminderssRecyclerView))
            .check(matches(hasDescendant(withText(reminder.title))))
        onView(withId(R.id.reminderssRecyclerView))
            .check(matches(hasDescendant(withText(reminder.description))))
        onView(withId(R.id.reminderssRecyclerView))
            .check(matches(hasDescendant(withText(reminder.location))))

        onView(withId(R.id.noDataTextView))
            .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }

    /**
     * @DrStart:     This test is used to check that the ReminderListFragment is displayed correctly.
     * */
    @Test
    fun reminderListFragment_noData_checkNoData() {
        // GIVEN: no data

        // WHEN: launch ReminderListFragment to display the list of reminders
        launchFragmentInContainer<ReminderListFragment>(themeResId = R.style.AppTheme)

        // THEN: check that the "no data" view is visible on screen

        onView(withId(R.id.noDataTextView))
            .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
        onView(withId(R.id.noDataTextView))
            .check(matches(withText(appContext.getString(R.string.no_data))))

    }

    /**
     * @DrStart:     This test is used to check that the ReminderListFragment is displayed correctly.
     * */
    @Test
    fun reminderListFragment_shouldReturnError() {
        /**
         * @DrStart:     Set the repository to return an error
         * */
        repository.setReturnError(true)

        /**
         * @DrStart:     Launch ReminderListFragment to display the list of reminders
         * */
        launchFragmentInContainer<ReminderListFragment>(themeResId = R.style.AppTheme)

        /**
         * @DrStart:     Check that the error message is displayed
         * */
        onView(withText("TestException"))
            .check(matches(isDisplayed()))
    }

    /**
     * @DrStart:     This test is used to check that the ReminderListFragment is displayed correctly.
     * */
    @Test
    fun reminderListFragment_navigateToSaveReminderFragment() {
        /**
         * @DrStart:     Launch ReminderListFragment to display the list of reminders
         * */
        val fragmentScenario =
            launchFragmentInContainer<ReminderListFragment>(themeResId = R.style.AppTheme)

        /**
         * @DrStart:     Navigate to SaveReminderFragment
         * */
        val navController = mock(NavController::class.java)
        fragmentScenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }
        onView(withId(R.id.addReminderFAB))
            .perform(click())

        /**
         * @DrStart:     Check that the navigation is correct
         * */
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

}
