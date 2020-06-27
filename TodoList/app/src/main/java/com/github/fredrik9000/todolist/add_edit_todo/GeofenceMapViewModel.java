package com.github.fredrik9000.todolist.add_edit_todo;

import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.SavedStateHandle;

import com.google.android.gms.maps.model.LatLng;

public class GeofenceMapViewModel extends AndroidViewModel {

    public static final String GEOFENCE_CENTER_LAT_STATE = "GEOFENCE_CENTER_LAT";
    public static final String GEOFENCE_CENTER_LONG_STATE = "GEOFENCE_CENTER_LONG";
    public static final String GEOFENCE_RADIUS_STATE = "GEOFENCE_RADIUS";
    public static final String HAS_SET_GEOFENCE_STATE = "HAS_SET_GEOFENCE";

    private LatLng geofenceCenter;
    private int geofenceRadius = GeofenceRadiusFragment.DEFAULT_RADIUS_IN_METERS;
    private boolean hasSetGeofence = false;
    private SavedStateHandle savedStateHandle;

    public GeofenceMapViewModel(@NonNull Application application, SavedStateHandle savedStateHandle) {
        super(application);
        this.savedStateHandle = savedStateHandle;
    }

    void setValuesFromArgumentsOrSavedState(Bundle args) {
        if (savedStateHandle.contains(HAS_SET_GEOFENCE_STATE)) {
            hasSetGeofence = savedStateHandle.get(HAS_SET_GEOFENCE_STATE);
            geofenceCenter = new LatLng((double) savedStateHandle.get(GEOFENCE_CENTER_LAT_STATE), (double) savedStateHandle.get(GEOFENCE_CENTER_LONG_STATE));
            geofenceRadius = savedStateHandle.get(GEOFENCE_RADIUS_STATE);
        } else {
            hasSetGeofence = args.getBoolean(GeofenceMapFragment.ARGUMENT_HAS_GEOFENCE_NOTIFICATION, false);
            if (hasSetGeofence) {
                geofenceRadius = args.getInt(GeofenceMapFragment.ARGUMENT_GEOFENCE_RADIUS, GeofenceRadiusFragment.DEFAULT_RADIUS_IN_METERS);
                geofenceCenter = new LatLng(args.getDouble(GeofenceMapFragment.ARGUMENT_GEOFENCE_LATITUDE, 0), args.getDouble(GeofenceMapFragment.ARGUMENT_GEOFENCE_LONGITUDE, 0));
            }
        }
    }

    void saveState() {
        savedStateHandle.set(HAS_SET_GEOFENCE_STATE, hasSetGeofence);
        savedStateHandle.set(GEOFENCE_CENTER_LAT_STATE, geofenceCenter != null ? geofenceCenter.latitude : 0);
        savedStateHandle.set(GEOFENCE_CENTER_LONG_STATE, geofenceCenter != null ? geofenceCenter.longitude : 0);
        savedStateHandle.set(GEOFENCE_RADIUS_STATE, geofenceRadius);
    }

    public LatLng getGeofenceCenter() {
        return geofenceCenter;
    }

    public void setGeofenceCenter(LatLng geofenceCenter) {
        this.geofenceCenter = geofenceCenter;
    }

    public int getGeofenceRadius() {
        return geofenceRadius;
    }

    public void setGeofenceRadius(int geofenceRadius) {
        this.geofenceRadius = geofenceRadius;
    }

    public boolean hasSetGeofence() {
        return hasSetGeofence;
    }

    public void setHasSetGeofence(boolean hasSetGeofence) {
        this.hasSetGeofence = hasSetGeofence;
    }
}
