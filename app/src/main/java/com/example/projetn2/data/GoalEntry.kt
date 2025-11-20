package com.example.projetn2.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goal_entries")
data class GoalEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String,
    val dailyGoal: Float,
    val timestamp: Long = System.currentTimeMillis()
)