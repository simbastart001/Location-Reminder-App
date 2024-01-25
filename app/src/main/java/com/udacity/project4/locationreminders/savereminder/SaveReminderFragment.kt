package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

/**
 * @DrStart:    The SaveReminderFragment allows the user to save a reminder.
 *              It displays a form with the reminder title, description, and location.
 *              The user can save the reminder by clicking on the save button.
 */
class SaveReminderFragment : BaseFragment() {

    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

    private lateinit var geofencingClient: GeofencingClient

    private var permissionsAndDeviceLocationEnabled = false
    private var isSavePending = false
    private var locationSnackbar: Snackbar? = null
    private var permissionsSnackbar: Snackbar? = null

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

        geofencingClient = LocationServices.getGeofencingClient(requireActivity())

        binding.selectLocation.setOnClickListener {
            val directions = SaveReminderFragmentDirections
                .actionSaveReminderFragmentToSelectLocationFragment()
            _viewModel.navigationCommand.value = NavigationCommand.To(directions)
        }

        enablePermissionsAndDeviceLocation()

        binding.saveReminder.setOnClickListener {
            if (permissionsAndDeviceLocationEnabled) {
                saveReminder()
            } else {
                // If permissions or location settings are not ready, indicate that a save is pending.
                isSavePending = true
                enablePermissionsAndDeviceLocation()
            }
        }
    }

    private fun saveReminder() {
        val title = _viewModel.reminderTitle.value
        val description = _viewModel.reminderDescription.value
        val location = _viewModel.reminderSelectedLocationStr.value
        val latitude = _viewModel.latitude.value
        val longitude = _viewModel.longitude.value

        val reminder = ReminderDataItem(title, description, location, latitude, longitude)

        // Validate and save the reminder, and then add a geofence.
        if (_viewModel.validateAndSaveReminder(reminder)) {
            addReminderGeofence(reminder)
        } else {
            Toast.makeText(
                requireContext(),
                "Please fill in all the fields to save the reminder.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * @DrStart:     Add a geofence associated to the reminder
     */
    @SuppressLint("MissingPermission")
    private fun addReminderGeofence(reminder: ReminderDataItem) {
        val geofenceDuration = 2 * 60 * 1000L // 2 minutes in milliseconds
        val geofence = Geofence.Builder()
            .setRequestId(reminder.id)
            .setCircularRegion(
                _viewModel.latitude.value!!,
                _viewModel.longitude.value!!,
                GEOFENCE_RADIUS_M
            )
            .setExpirationDuration(geofenceDuration)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        geofencingClient.addGeofences(
            geofencingRequest,
            (activity as RemindersActivity).geofencePendingIntent
        ).run {
            addOnSuccessListener {
                Log.i(
                    "Debugging..",
                    "Successful geofencing request: geofence added to the map"
                )
            }
            addOnFailureListener {
                Toast.makeText(
                    requireContext(),
                    "Failed geofencing request: ${it.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (permissionsSnackbar != null && permissionsSnackbar!!.isShown)
            permissionsSnackbar!!.dismiss()
        if (locationSnackbar != null && locationSnackbar!!.isShown)
            locationSnackbar!!.dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        _viewModel.onClear()
    }

    /**
     * @DrStart:     Check if permissions are granted: if not, request them
     */
    @SuppressLint("MissingPermission")
    private fun enablePermissionsAndDeviceLocation() {
        if (arePermissionsGranted()) {
            checkDeviceLocationSettings()
        } else {
            var permissionArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            val resultCode =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    permissionArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    FOREGROUND_AND_BACKGROUND_PERMISSIONS_REQUEST_CODE
                } else ONLY_FOREGROUND_PERMISSION_REQUEST_CODE
            requestPermissions(permissionArray, resultCode)
        }
    }

    private fun arePermissionsGranted(): Boolean {
        val foregroundPermissionApproved =
            (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ))
        val backgroundPermissionApproved =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            } else true

        return (foregroundPermissionApproved && backgroundPermissionApproved)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.isEmpty() ||
            grantResults[0] == PackageManager.PERMISSION_DENIED ||
            requestCode == FOREGROUND_AND_BACKGROUND_PERMISSIONS_REQUEST_CODE &&
            grantResults[1] == PackageManager.PERMISSION_DENIED
        ) {

            permissionsSnackbar = Snackbar.make(
                binding.root,
                R.string.permission_denied_explanation,
                Snackbar.LENGTH_INDEFINITE
            )
            permissionsSnackbar!!.setAction(R.string.settings) {
                startActivity(Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
                permissionsSnackbar!!.dismiss()
            }
                .show()
        } else
            checkDeviceLocationSettings()
    }

    /**
     * @DrStart:     Check if device location is enabled: if not, request it
     */
    private fun checkDeviceLocationSettings(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireContext())

        val locationSettingsResponseTask = settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener {
            if (it is ResolvableApiException && resolve) {
                try {
                    startIntentSenderForResult(
                        it.resolution.intentSender,
                        TURN_DEVICE_LOCATION_ON_REQUEST_CODE,
                        null, 0, 0, 0, null
                    )
                } catch (sendException: IntentSender.SendIntentException) {
                    Toast.makeText(
                        requireContext(),
                        "Error getting location settings resolution: ${sendException.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                locationSnackbar = Snackbar.make(
                    binding.root,
                    R.string.location_required_error,
                    Snackbar.LENGTH_INDEFINITE
                )
                locationSnackbar!!.setAction(android.R.string.ok) {
                    checkDeviceLocationSettings()
                    locationSnackbar!!.dismiss()
                }.show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                permissionsAndDeviceLocationEnabled = true
                if (isSavePending) {
                    saveReminder()
                    isSavePending = false
                }
            } else {
                // Handle the case where the location settings are not satisfied.
                // If the user can fix this situation, you should prompt them to do so.
                if (resolve && task.exception is ResolvableApiException) {
                    // Prompt the user to change location settings.
                    val resolvable = task.exception as ResolvableApiException
                    startIntentSenderForResult(
                        resolvable.resolution.intentSender,
                        TURN_DEVICE_LOCATION_ON_REQUEST_CODE,
                        null, 0, 0, 0, null
                    )
                } else {
                    // Location settings are not satisfied, but we have no way to fix the settings so we won't prompt the user.
                    permissionsAndDeviceLocationEnabled = false
                }
            }
        }
//        end
    }

    /**
     * @DrStart:     Check if the device location is enabled after the user has been prompted to enable it
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == TURN_DEVICE_LOCATION_ON_REQUEST_CODE)
            checkDeviceLocationSettings(false)
    }

    companion object {
        private const val GEOFENCE_RADIUS_M = 400f
        private const val FOREGROUND_AND_BACKGROUND_PERMISSIONS_REQUEST_CODE = 401
        private const val ONLY_FOREGROUND_PERMISSION_REQUEST_CODE = 402
        private const val TURN_DEVICE_LOCATION_ON_REQUEST_CODE = 403
    }
}
