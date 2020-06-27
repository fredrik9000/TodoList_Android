package com.github.fredrik9000.todolist.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;

import com.github.fredrik9000.todolist.R;
import com.github.fredrik9000.todolist.model.TodoDao;
import com.github.fredrik9000.todolist.model.TodoDatabase;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

public class GeofenceReceiver extends BroadcastReceiver {

    public static final String TAG = "GeofenceReceiver";

    public static final String TODO_DESCRIPTION = "TODO_DESCRIPTION";
    public static final String NOTIFICATION_ID = "NOTIFICATION_ID";

    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Log.e(TAG, errorMessage(context.getResources(), geofencingEvent.getErrorCode()));
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            int notificationId = intent.getIntExtra(NOTIFICATION_ID, 0);
            NotificationUtil.sendNotification(context, context.getResources().getString(R.string.geofence_notification_title), intent.getStringExtra(TODO_DESCRIPTION), R.drawable.ic_baseline_location_searching_black_24dp, notificationId);
            new DisableGeofenceNotificationWithIdAsyncTask(TodoDatabase.getInstance(context.getApplicationContext()).todoDao()).execute(notificationId);
            NotificationUtil.removeGeofenceNotification(context.getApplicationContext(), notificationId);
        } else {
            Log.e(TAG, context.getResources().getString(R.string.geofence_transition_invalid_type, geofenceTransition));
        }
    }

    private String errorMessage(Resources resources, int errorCode) {
        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return resources.getString(R.string.geofence_not_available);
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return resources.getString(R.string.too_many_geofences);
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return resources.getString(R.string.too_many_pending_intents);
            default:
                return resources.getString(R.string.unknown_geofence_error);
        }
    }

    private static class DisableGeofenceNotificationWithIdAsyncTask extends AsyncTask<Integer, Void, Void> {
        private TodoDao todoDao;

        private DisableGeofenceNotificationWithIdAsyncTask(TodoDao todoDao) {
            this.todoDao = todoDao;
        }

        @Override
        protected Void doInBackground(Integer... notificationIds) {
            todoDao.disableGeofenceNotificationWithId(notificationIds[0]);
            return null;
        }
    }
}
