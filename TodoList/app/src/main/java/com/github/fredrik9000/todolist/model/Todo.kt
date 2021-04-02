package com.github.fredrik9000.todolist.model

import android.content.Context
import android.location.Geocoder
import android.util.Log
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.IOException
import java.text.DateFormat
import java.util.*

@Entity(tableName = "todo_table")
class Todo(@PrimaryKey(autoGenerate = true) var id: Int = 0,
           var title: String? = null,
           var description: String? = null,
           var priority: Int = 0,
           var notificationId: Int = 0,
           var geofenceNotificationId: Int = 0,
           var notificationEnabled: Boolean = false,
           var notifyYear: Int = 0,
           var notifyMonth: Int = 0,
           var notifyDay: Int = 0,
           var notifyHour: Int = 0,
           var notifyMinute: Int = 0,
           var geofenceNotificationEnabled: Boolean = false,
           var geofenceLatitude: Double = 0.0,
           var geofenceLongitude: Double = 0.0,
           var geofenceRadius: Int = 0,
           var isCompleted: Boolean = false) {

    companion object {
        private const val TAG: String = "Todo"

        @JvmStatic
        fun getAddressFromLatLong(context: Context, latitude: Double, longitude: Double, geofenceNotificationEnabled: Boolean): String {
            if (!geofenceNotificationEnabled) {
                return ""
            }

            var address: String? = null
            try {
                address = Geocoder(context, Locale.getDefault()).getFromLocation(latitude, longitude, 1)[0].getAddressLine(0)
            } catch (e: IOException) {
                Log.w(TAG, "Could not get city from latitude and longitude: " + e.message)
            }
            return address ?: "Unknown"
        }

        @JvmStatic
        fun getPrettifiedDateAndTime(todoItem: Todo): String? {
            with(Calendar.getInstance()) {
                this[todoItem.notifyYear, todoItem.notifyMonth, todoItem.notifyDay, todoItem.notifyHour, todoItem.notifyMinute] = 0
                return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.US).format(this.time)
            }
        }
    }
}