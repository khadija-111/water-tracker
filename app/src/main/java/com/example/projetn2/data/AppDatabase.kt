package com.example.projetn2.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [WaterEntry::class, GoalEntry::class], version = 3)
abstract class AppDatabase : RoomDatabase() {

    abstract fun waterDao(): WaterDao
    abstract fun goalDao(): GoalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "water_db"
                )
                    .fallbackToDestructiveMigration() // لتجنب الكراش عند تغيير الـ schema
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}