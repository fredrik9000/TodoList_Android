package com.github.fredrik9000.todolist.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.util.Log
import com.github.fredrik9000.todolist.R
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent

class GeofenceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            Log.e(TAG, errorMessage(context.resources, geofencingEvent.errorCode))
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            GeofenceNotificationJobIntentService.enqueueWork(
                context = context,
                work = Intent(context, GeofenceNotificationJobIntentService::class.java).apply {
                    putIntegerArrayListExtra(NOTIFICATION_ID, ArrayList(geofencingEvent.triggeringGeofences.map { it.requestId.toInt() }))
                }
            )
        } else {
            Log.e(TAG, context.resources.getString(R.string.geofence_transition_invalid_type, geofenceTransition))
        }
    }

    private fun errorMessage(resources: Resources, errorCode: Int): String {
        return when (errorCode) {
            GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> resources.getString(R.string.geofence_not_available)
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> resources.getString(R.string.too_many_geofences)
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> resources.getString(R.string.too_many_pending_intents)
            else -> resources.getString(R.string.unknown_geofence_error)
        }
    }

    companion object {
        private const val TAG = "GeofenceReceiver"
        const val NOTIFICATION_ID = "NOTIFICATION_ID"
    }
}