package com.github.fredrik9000.todolist.notifications

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavDeepLinkBuilder
import com.github.fredrik9000.todolist.App
import com.github.fredrik9000.todolist.R
import com.github.fredrik9000.todolist.add_edit_todo.AddEditTodoFragment
import com.github.fredrik9000.todolist.model.Todo
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import java.util.*

object NotificationUtil {

    private const val TAG: String = "NotificationUtil"
    private const val GEOFENCE_REQUEST_CODE = 1001

    fun removeNotification(applicationContext: Context?, alarmManager: AlarmManager, notificationId: Int) {
        val notificationIntent = Intent(applicationContext, TimedNotificationAlarmReceiver::class.java)
        val pendingIntent = getNotificationPendingIntent(
            applicationContext = applicationContext,
            notificationId = notificationId,
            notificationIntent = notificationIntent
        )

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    fun addNotification(
        applicationContext: Context?,
        alarmManager: AlarmManager,
        notificationId: Int,
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int
    ) {
        val notificationIntent = Intent(applicationContext, TimedNotificationAlarmReceiver::class.java).apply {
            putExtra(TimedNotificationAlarmReceiver.NOTIFICATION_ID, notificationId)
        }

        with(Calendar.getInstance()) {
            this[year, month, day, hour, minute] = 0
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                this.timeInMillis,
                getNotificationPendingIntent(applicationContext, notificationId, notificationIntent)
            )
        }
    }

    fun removeGeofence(applicationContext: Context, notificationId: Int) {
        removeGeofenceList(applicationContext, listOf(notificationId.toString()))
    }

    fun removeGeofenceList(applicationContext: Context, geofenceNotificationIdList: List<String>) {
        LocationServices.getGeofencingClient(applicationContext).removeGeofences(geofenceNotificationIdList)
            .addOnSuccessListener { Log.e(TAG, applicationContext.resources.getString(R.string.geofence_removed)) }
            .addOnFailureListener { Log.e(TAG, applicationContext.resources.getString(R.string.geofence_removal_failed)) }
    }

    @SuppressLint("MissingPermission")
    fun addGeofenceNotification(
        applicationContext: Context,
        notificationId: Int,
        radius: Int,
        latitude: Double,
        longitude: Double
    ) {
        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(0) // Don't trigger geofence if already inside it
            .addGeofences(
                listOf(
                    Geofence.Builder()
                        .setRequestId(notificationId.toString())
                        .setCircularRegion(latitude, longitude, radius.toFloat())
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .setTransitionTypes(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                        .build()
                )
            )
            .build()

        LocationServices.getGeofencingClient(applicationContext).addGeofences(
            geofencingRequest,
            getNotificationPendingIntent(applicationContext, GEOFENCE_REQUEST_CODE, Intent(applicationContext, GeofenceReceiver::class.java))
        )
            .addOnSuccessListener {
                Log.e(TAG, applicationContext.resources.getString(R.string.geofence_added))
            }
            .addOnFailureListener {
                Log.e(TAG, applicationContext.resources.getString(R.string.adding_geofence_failed))
            }
    }

    private fun getNotificationPendingIntent(applicationContext: Context?, notificationId: Int, notificationIntent: Intent): PendingIntent {
        // Using FLAG_UPDATE_CURRENT so that we get the same pending intent back when adding and removing
        val intentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        return PendingIntent.getBroadcast(applicationContext, notificationId, notificationIntent, intentFlags)
    }

    fun sendNotification(context: Context, title: String?, todo: Todo, icon: Int, notificationId: Int) {
        val pendingIntent = NavDeepLinkBuilder(context)
            .setGraph(R.navigation.navigation_graph)
            .setDestination(R.id.addEditTodoFragment)
            .setArguments(AddEditTodoFragment.createBundleForTodoItem(todo))
            .createPendingIntent()

        val notification = NotificationCompat.Builder(context, App.NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(todo.title)
            .setSmallIcon(icon)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
            .setLights(Color.RED, 3000, 3000)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
}