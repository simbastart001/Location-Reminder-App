package com.udacity.project4

import android.app.Activity
import android.app.Application
import androidx.navigation.NavController
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import org.koin.test.get
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.hamcrest.core.Is.`is`
import org.hamcrest.core.IsNot.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest

@RunWith(AndroidJUnit4::class)
@LargeTest
class RemindersActivityTest : KoinTest {


    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    //get hold of the activity context
    private fun getActivity(activityScenario: ActivityScenario<RemindersActivity>): Activity? {
        var activity: Activity? = null
        activityScenario.onActivity {
            activity = it
        }
        return activity
    }

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

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    /**
     * @DrStart:     This function is used to do end to end test. It creates a reminder and checks
     *            the reminders list. It checks for toast message and snackbar message.
     * */
    @Test
    fun addNewReminderListUpdatedWithNewReminder() = runBlocking {

        // GIVEN
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        val activity = getActivity(activityScenario)

        // WHEN
        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).perform(replaceText("ISTART HQ"))
        onView(withId(R.id.reminderDescription)).perform(replaceText("Get your bugs fixed"))
        onView(withId(R.id.selectLocation)).perform(click())
        onView(withId(R.id.theMap)).perform(click())
        onView(withId(R.id.save_button)).perform(click())
        onView(withId(R.id.saveReminder)).perform(click())

        // THEN
        //Check whether the reminder saved toast message is displayed
        onView(withText(R.string.reminder_saved)).inRoot(
            withDecorView(
                not(
                    `is`(
                        activity!!.window.decorView
                    )
                )
            )
        ).check(matches(isDisplayed()))

        runBlocking {
            delay(6000)
        }

    }

    /**
     * @DrStart:     This function is used to do end to end test. It creates a reminder and checks
     *            the reminders list. It checks for toast message and snackbar message.
     * */
    @Test
    fun addNewReminder_NoTitle_ShowsSnackbar() = runBlocking {

        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // GIVEN - an SaveReminder Screen without Selected Location
        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).perform(replaceText("Title"))
        onView(withId(R.id.reminderDescription)).perform(replaceText("Description"))
        //AND Missing location

        // WHEN - click saveButton
        onView(withId(R.id.saveReminder)).perform(click())

        // THEN - shows Snackbar
        onView(withId(com.google.android.material.R.id.snackbar_text)).check(
            matches(isDisplayed())
        )

        runBlocking {
            delay(2000)
        }
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
    fun createReminderAndCheckList() = runBlocking {
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
        onView(withText(reminder.title)).check(matches(isDisplayed()))

        //make sure activity is closed before resetting the db
        activityScenario.close()

        // Delay
        runBlocking {
            delay(2000)
        }
    }
}
