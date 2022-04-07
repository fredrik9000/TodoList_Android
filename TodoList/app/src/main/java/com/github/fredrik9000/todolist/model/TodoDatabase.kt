package com.github.fredrik9000.todolist.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Todo::class], version = 1)
abstract class TodoDatabase : RoomDatabase() {

    abstract fun todoDao(): TodoDao

    companion object {
        @Volatile
        private var instance: TodoDatabase? = null

        @Synchronized
        fun getInstance(context: Context): TodoDatabase =
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                TodoDatabase::class.java,
                "todo_database"
            )
                .fallbackToDestructiveMigration()
                .build().also { instance = it }
    }
}