package com.github.fredrik9000.todolist.add_edit_todo.add_edit_geofence

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import com.google.android.gms.maps.model.LatLng

class GeofenceMapViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    var geofenceCenter: LatLng = LatLng(0.0, 0.0)
    var geofenceRadius: Int = GeofenceRadiusFragment.DEFAULT_RADIUS_IN_METERS
    var hasSetGeofence = false

    fun setValuesFromArgumentsOrSavedState(args: Bundle) {
        if (savedStateHandle.contains(HAS_SET_GEOFENCE_STATE)) {
            hasSetGeofence = savedStateHandle[HAS_SET_GEOFENCE_STATE]!!
            geofenceCenter = LatLng(
                savedStateHandle[GEOFENCE_CENTER_LAT_STATE]!!,
                savedStateHandle[GEOFENCE_CENTER_LONG_STATE]!!
            )
            geofenceRadius = savedStateHandle[GEOFENCE_RADIUS_STATE]!!
        } else {
            hasSetGeofence = args.getBoolean(GeofenceMapFragment.ARGUMENT_HAS_GEOFENCE_NOTIFICATION, false)
            if (hasSetGeofence) {
                geofenceRadius = args.getInt(
                    GeofenceMapFragment.ARGUMENT_GEOFENCE_RADIUS,
                    GeofenceRadiusFragment.DEFAULT_RADIUS_IN_METERS
                )
                geofenceCenter = LatLng(
                    args.getDouble(GeofenceMapFragment.ARGUMENT_GEOFENCE_LATITUDE, 0.0),
                    args.getDouble(GeofenceMapFragment.ARGUMENT_GEOFENCE_LONGITUDE, 0.0)
                )
            }
        }
    }

    fun saveState() {
        savedStateHandle[HAS_SET_GEOFENCE_STATE] = hasSetGeofence
        savedStateHandle[GEOFENCE_CENTER_LAT_STATE] = geofenceCenter.latitude
        savedStateHandle[GEOFENCE_CENTER_LONG_STATE] = geofenceCenter.longitude
        savedStateHandle[GEOFENCE_RADIUS_STATE] = geofenceRadius
    }

    companion object {
        const val GEOFENCE_CENTER_LAT_STATE = "GEOFENCE_CENTER_LAT"
        const val GEOFENCE_CENTER_LONG_STATE = "GEOFENCE_CENTER_LONG"
        const val GEOFENCE_RADIUS_STATE = "GEOFENCE_RADIUS"
        const val HAS_SET_GEOFENCE_STATE = "HAS_SET_GEOFENCE"
    }
}