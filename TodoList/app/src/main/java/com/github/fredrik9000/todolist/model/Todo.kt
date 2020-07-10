package com.github.fredrik9000.todolist.model

import androidx.room.Entity
import androidx.room.PrimaryKey

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
           var isCompleted: Boolean = false)