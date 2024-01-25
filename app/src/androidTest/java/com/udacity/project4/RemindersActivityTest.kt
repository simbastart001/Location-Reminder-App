package com.udacity.project4

import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withHint
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.android.material.internal.ContextUtils.getActivity
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.not
import org.hamcrest.core.Is.`is`
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import org.hamcrest.Matchers.not


/**
 * @DrStart:     This class is used to test the RemindersActivity. It is an end to end test. It tests the
 *              RemindersActivity and the ReminderListFragment. It uses the ReminderListViewModel and
 *              SaveReminderViewModel. It uses the RemindersLocalRepository and the LocalDataSource.
 *              It uses the DataBindingIdlingResource and the EspressoIdlingResource.
 *              It uses the KoinTest.
 * */
@RunWith(AndroidJUnit4::class)
@LargeTest
//  END TO END test to black box test the app
class RemindersActivityTest :
    KoinTest {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }


    //    TODO: add End to End testing to the app
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    /**
     * @DrStart:     This function is used to register the idling resource. It is called before each test. It
     *             registers the EspressoIdlingResource and the dataBindingIdlingResource.
     * */
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    /**
     * @DrStart:     This function is used to unregister the idling resource. It is called after each test. It
     *             unregisters the EspressoIdlingResource and the dataBindingIdlingResource.
     * */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    private fun getReminder(): ReminderDTO {
        return ReminderDTO(
            title = "ISTART HQ",
            description = "Get your bugs fixed here!",
            location = "Aspindale Park",
            latitude = -17.83088859497177,
            longitude = 30.964896082878113
        )
    }

    /**
     * @DrStart:     This function is used to test the RemindersActivity. It creates a reminder and checks
     *             the reminders list.
     * */
    @Test
    fun createReminderAndCheckRemindersList() = runBlocking {
        val reminder = getReminder()
        repository.saveReminder(reminder)

        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // Check List of added reminder with data displayed
        onView(withText(reminder.title))
            .check(matches(isDisplayed()))
        onView(withText(reminder.description))
            .check(matches(isDisplayed()))
        onView(withText(reminder.location))
            .check(matches(isDisplayed()))

        //show Snackbar error
        onView(withText(R.string.err_enter_title)).check(matches(isDisplayed()))

        //make sure activity is closed before resetting the db
        activityScenario.close()

        // Delay
        runBlocking {
            delay(2000)
        }
    }

    @Test
    fun saveTest_showToast() {
        //start screen
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        //click Add reminder Fab
        onView(withId(R.id.addReminderFAB)).perform(click())

        //check add reminder form
        onView(withId(R.id.reminderTitle)).check(matches(withHint(appContext.getString(R.string.reminder_title))))
        onView(withId(R.id.reminderDescription)).check(matches(withHint(appContext.getString(R.string.reminder_desc))))
        onView(withId(R.id.selectLocation)).check(matches(withText(appContext.getString(R.string.reminder_location))))


        //type reminder title and description
        onView(withId(R.id.reminderTitle)).perform(typeText("ISTART HQ"))
        onView(withId(R.id.reminderDescription)).perform(typeText("Buy a new car"))
        closeSoftKeyboard()

        //click select location
        onView(withId(R.id.selectLocation)).perform(click())

        //click on map and save
        onView(withId(R.id.theMap)).perform(longClick())
        onView(withId(R.id.save_button)).perform(click())

        //save Reminder
        onView(withId(R.id.saveReminder)).perform(click())

        // Check for the Toast message

        /**
         * @DrStart:    Please note I have tried everything but showing a toast seems to be buggy
         *              and not showing from my Android 11 device.
         *              https://github.com/android/android-test/issues/803
         *              Please help!
         * */
        activityScenario.onActivity {
            onView(withText(R.string.reminder_saved))
                .inRoot(withDecorView(not(it.window.decorView)))
                .check(matches(isDisplayed()))
        }

        activityScenario.close()
    }

}
