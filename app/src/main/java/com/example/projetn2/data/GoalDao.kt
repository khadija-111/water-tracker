package com.example.projetn2.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface GoalDao {

    @Query("SELECT * FROM goal_entries WHERE userEmail = :email ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestGoal(email: String): GoalEntry?

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertGoal(goal: GoalEntry)

    @Query("SELECT * FROM goal_entries WHERE userEmail = :email ORDER BY timestamp DESC")
    suspend fun getAllGoals(email: String): List<GoalEntry>
}