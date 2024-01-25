package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.launch

class SaveReminderViewModel(val app: Application, val dataSource: ReminderDataSource) :
    BaseViewModel(app) {
    val reminderTitle = MutableLiveData<String?>()
    val reminderDescription = MutableLiveData<String?>()
    val reminderSelectedLocationStr = MutableLiveData<String?>()
    val selectedPOI = MutableLiveData<PointOfInterest?>()
    val latitude = MutableLiveData<Double?>()
    val longitude = MutableLiveData<Double?>()

    fun onClear() {
        reminderTitle.value = null
        reminderDescription.value = null
        reminderSelectedLocationStr.value = null
        selectedPOI.value = null
        latitude.value = null
        longitude.value = null
    }

    /**
     * @DrStart:     Save the reminder to the local data source
     */
    fun fillReminderLocationParameters(
        locationString: String?,
        poi: PointOfInterest?,
        lat: Double?,
        long: Double?
    ) {
        reminderSelectedLocationStr.value = locationString
        selectedPOI.value = poi
        latitude.value = lat
        longitude.value = long
    }

    /**
     * @DrStart:     Validate the entered data and save the reminder to the data source
     */
    fun validateAndSaveReminder(reminderData: ReminderDataItem): Boolean {
        return if (validateEnteredData(reminderData)) {
            saveReminder(reminderData)
            true
        } else false
    }

    fun saveReminder(reminderData: ReminderDataItem) {
        showLoading.value = true
        viewModelScope.launch {
            dataSource.saveReminder(
                ReminderDTO(
                    reminderData.title,
                    reminderData.description,
                    reminderData.location,
                    reminderData.latitude,
                    reminderData.longitude,
                    reminderData.id
                )
            )
            showLoading.value = false
            showToast.value = app.getString(R.string.reminder_saved)
            navigationCommand.value = NavigationCommand.Back
        }
    }

    /**
     * @DeStart:     Validate the entered data for the reminder
     */
    private fun validateEnteredData(reminderData: ReminderDataItem): Boolean {
        if (reminderData.title.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_enter_title
            return false
        }

        if (reminderData.location.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_select_location
            return false
        }
        return true
    }

    /**
     * @DrStart:     Add method to delete a single reminder from the data source
     */

    fun deleteReminder(id: String) {
        showLoading.value = true
        viewModelScope.launch {
            val result = dataSource.deleteReminder(id)
            showLoading.value = false
            when (result) {
                is Result.Success -> {
                    showSnackBarInt.value = R.string.reminder_deleted
                    navigationCommand.value = NavigationCommand.Back
                }

                is Result.Error -> showSnackBar.value = result.message
            }
        }
    }

    fun backToPreviousFragment() {
        navigationCommand.value = NavigationCommand.Back
    }
}
