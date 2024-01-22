package com.udacity.project4.locationreminders

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityReminderDescriptionBinding
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

/**
 * Activity that displays the reminder details after the user clicks on the notification
 */
class ReminderDescriptionActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityReminderDescriptionBinding
    private lateinit var mapView: MapView
    private lateinit var map: GoogleMap
    private lateinit var reminder: ReminderDataItem

    companion object {
        private const val EXTRA_ReminderDataItem = "EXTRA_ReminderDataItem"

        /**
         * @DrStart:     Creates a new intent that can be used to start ReminderDescriptionActivity
         */
        fun newIntent(context: Context, reminderDataItem: ReminderDataItem): Intent {
            val intent = Intent(context, ReminderDescriptionActivity::class.java)
            intent.putExtra(EXTRA_ReminderDataItem, reminderDataItem)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layoutId = R.layout.activity_reminder_description
        binding = DataBindingUtil.setContentView(this, layoutId)

        // Retrieve the reminder data from the intent extras
        reminder = intent.getSerializableExtra(EXTRA_ReminderDataItem) as ReminderDataItem

        binding.apply {
            lifecycleOwner = this@ReminderDescriptionActivity
            reminderDataItem = reminder
        }

        // Set the mapView
        mapView = binding.mapMarker
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onMapReady(p0: GoogleMap) {
        map = p0
        val reminderMapLocation = LatLng(reminder.latitude!!, reminder.longitude!!)

        map.addMarker(MarkerOptions().position(reminderMapLocation).title(reminder.description!!))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(reminderMapLocation, 15f))
    }

    override fun onBackPressed() {
        navigateBackToRemindersList()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                navigateBackToRemindersList()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * @DrStart:    Navigate back to the RemindersActivity
     */
    private fun navigateBackToRemindersList() {
        // Check if the main activity, RemindersActivity, is already present in the back stack
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val taskList = activityManager.getRunningTasks(Int.MAX_VALUE)
        for (task in taskList) {
            if (task.baseActivity?.className == RemindersActivity::class.java.name) {
                // Instance found: go back by simply popping off this activity
                finish()
                return
            }
        }
        val intent = Intent(this, RemindersActivity::class.java)
        Log.i("Debugging..", "RemindersActivity not found. Starting a new instance.")
        startActivity(intent)
        finish()
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
}
