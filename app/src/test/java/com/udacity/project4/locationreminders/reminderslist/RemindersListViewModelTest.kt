//package com.udacity.project4.locationreminders.reminderslist
//
//import androidx.arch.core.executor.testing.InstantTaskExecutorRule
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import com.udacity.project4.locationreminders.MainCoroutineRule
//import com.udacity.project4.locationreminders.data.RemindersRepositoryTest
//import com.udacity.project4.locationreminders.data.local.RemindersLocalRepositoryTest
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import org.junit.Before
//import org.junit.Rule
//import org.junit.runner.RunWith
//
//@RunWith(AndroidJUnit4::class)
//@ExperimentalCoroutinesApi
//class RemindersListViewModelTest {
//
//    @get: Rule
//    var mainCoroutineRule = MainCoroutineRule()
//
//    // subject under test
//    private lateinit var remindersListViewModel: RemindersListViewModel
//
//    // Use a fake repository to be injected into the viewmodel
//    private lateinit var remindersRepository: RemindersLocalRepositoryTest
//
//    @get: Rule
//    var instantExecutorRule = InstantTaskExecutorRule()
//
//    @Before
//    fun setupViewModel() {
//        remindersRepository = RemindersRepositoryTest()
//        val reminder1 = ReminderDataItem("Title1", "Description1", "Location1", -17.87382659012941, 31.029769897460938)
//        val reminder2 = ReminderDataItem("Title2", "Description2", "Location2", -17.87002039700178, 31.029769897460938)
//
//        remindersRepository.add(reminder1)
//    }
//
//
//
//}