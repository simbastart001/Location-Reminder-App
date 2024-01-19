package com.udacity.project4.locationreminders.savereminder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {

    // Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val layoutId = R.layout.fragment_save_reminder
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)

        setDisplayHomeAsUpEnabled(true)
        binding.viewModel = _viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            // Navigate to another fragment to get the user location
            val directions = SaveReminderFragmentDirections
                .actionSaveReminderFragmentToSelectLocationFragment()
            _viewModel.navigationCommand.value = NavigationCommand.To(directions)
        }

//        binding.saveReminder.setOnClickListener {
//            val title = _viewModel.reminderTitle.value
//            val description = _viewModel.reminderDescription
//            val location = _viewModel.reminderSelectedLocationStr.value
//            val latitude = _viewModel.latitude
//            val longitude = _viewModel.longitude.value
//
//            // TODO: use the user entered reminder details to:
//            //  1) add a geofencing request
//            //  2) save the reminder to the local db
//        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value

            // Validate the input
            if (_viewModel.validateEnteredData(
                    ReminderDataItem(
                        title,
                        description,
                        location,
                        latitude,
                        longitude
                    )
                )
            ) {
                // TODO: add a geofencing request
//                addGeofencingRequest(latitude, longitude, title)

                // Save the reminder to the local db
                _viewModel.saveReminder(
                    ReminderDataItem(
                        title,
                        description,
                        location,
                        latitude,
                        longitude
                    )
                )
            }
        }

    }

    private fun addGeofencingRequest(latitude: Double?, longitude: Double?, title: String?) {
        // Implementation details for adding a geofence will depend on the APIs you're using.
        // Typically, this will involve creating a Geofence object, adding it to a GeofencingRequest,
        // and then registering that request with a GeofencingClient.
    }

    override fun onDestroy() {
        super.onDestroy()
        // Make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }
}