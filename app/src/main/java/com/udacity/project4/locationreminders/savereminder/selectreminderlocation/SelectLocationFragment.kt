package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.Manifest
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.Locale

/**
 * @DrStart:    The SelectLocationFragment allows the user to select a location for the reminder.
 *             It displays a map with a marker at the selected location.
 *             The user can select a location by clicking on the map or on a point of interest.
 *             The user can save the reminder by clicking on the save button.
 */
class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    // Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var mapView: MapView
    private lateinit var map: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val layoutId = R.layout.fragment_select_location
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        mapView = binding.theMap
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

//        // Check and request location permission
//        checkLocationPermission()

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        return binding.root
    }

    override fun onMapReady(p0: GoogleMap) {
        map = p0
        setMapStyle()
        setMapClick()
        setMapPoiClick()

        // Check and request location permission
        checkLocationPermission()

    }

    private fun setMapStyle() {
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style))
    }

    private fun setMapClick() {

        map.setOnMapClickListener {
            // Remove previous markers from the map
            map.clear()

            // Add new marker
            val locationString = String.format(
                Locale.getDefault(),
                "Lat %1$.3f, Long %2$.3f",
                it.latitude,
                it.longitude
            )
            map.addMarker(MarkerOptions().position(it).title(locationString))

            _viewModel.fillReminderLocationParameters(
                locationString, null, it.latitude, it.longitude
            )
        }
    }

    private fun setMapPoiClick() {

        map.setOnPoiClickListener {
            // Remove previous markers from the map
            map.clear()

            // Add new marker
            map.addMarker(MarkerOptions().position(it.latLng).title(it.name))

            _viewModel.fillReminderLocationParameters(
                it.name, it, it.latLng.latitude, it.latLng.longitude
            )
        }
    }

    /**
     * @DrStart:     Move to the user current location when the map is ready
     */
    @SuppressLint("MissingPermission")
    private fun moveToCurrentLocation() {
        map.isMyLocationEnabled = true
        LocationServices.getFusedLocationProviderClient(requireContext()).lastLocation
            .addOnSuccessListener {
                if (it != null) {
                    map.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(it.latitude, it.longitude), 15f
                        )
                    )
                }
            }
    }

    /**
     * @DrStart:     Check and request location permission
     */
    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission granted, enable My Location layer
                enableMyLocation()
            }

            else -> {
                // Request permission
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    /**
     * @DrStart:     Enable My Location layer if the permission has been granted. Otherwise, display a
     *            snackbar explaining that the user needs location permissions in order to play.
     */
    private fun enableMyLocation() {

        //check permission
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Snackbar.make(
                binding.saveButton,
                R.string.permission_denied_explanation,
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.settings) {
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()

            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        //TODO: zoom to the user location after taking his permission
        map.setMyLocationEnabled(true)
        moveToCurrentLocation()

    }


    /**
     * @DrStart:     Show a Snackbar explaining why location permission is required
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, enable My Location layer
                    enableMyLocation()
                } else {
                    // Permission denied, show error message
                    showPermissionDeniedError()
                }
            }

            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    /**
     * @DrStart:     Show a Snackbar explaining why location permission is required
     */
    private fun showPermissionDeniedError() {
        Snackbar.make(
            binding.root,
            "Location permission is required!",
            Snackbar.LENGTH_LONG
        ).show()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // TODO: Change the map type based on the user's selection.
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }

        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }

        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }

        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }

        else -> super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}
