package com.udacity.project4.locationreminders.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.locationreminders.RemindersActivity

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        /**
         * @DrStart:    Return if the intent action is not valid from the RemindersActivity.kt file.
         * */
        if (intent.action != RemindersActivity.ACTION_GEOFENCE_EVENT) {
            return
        }
        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return

        /**
         * @DrStart:      Return if the geofencing event has an error, if the geofence transition is not
         *                a geofence enter event, or if the triggering geofences are null or empty.
         *                Otherwise, enqueue the work.
         * */
        if (
            geofencingEvent.hasError() ||
            geofencingEvent.geofenceTransition != Geofence.GEOFENCE_TRANSITION_ENTER ||
            geofencingEvent.triggeringGeofences.isNullOrEmpty()
        ) {
            Log.e("GeofenceCheck..", "Incorrect geofencing event triggered")
            return
        }
        GeofenceTransitionsJobIntentService.enqueueWork(context, intent)
    }
}

