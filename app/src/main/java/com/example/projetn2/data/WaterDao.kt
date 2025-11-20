package com.example.projetn2.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface WaterDao {

    // جلب جميع المداخل ديال مستخدم محدد
    @Query("SELECT * FROM water_entries WHERE userEmail = :email")
    suspend fun getAllByUser(email: String): List<WaterEntry>

    // جلب يوم محدد لمستخدم محدد
    @Query("SELECT * FROM water_entries WHERE userEmail = :email AND dayIndex = :index")
    suspend fun getByDay(email: String, index: Int): WaterEntry?

    // إدخال أو تحديث تلقائي إذا كان موجود
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(entry: WaterEntry)

    @Update
    suspend fun update(entry: WaterEntry)

    // حذف سجل محدد
    @Delete
    suspend fun delete(entry: WaterEntry)

    @Query("SELECT SUM(amount) FROM water_entries WHERE userEmail = :email AND dayIndex = :dayIndex")
    suspend fun getTotalForToday(email: String, dayIndex: Int): Float?
    // تجبد آخر 7 سجلات لكل المستخدم، مرتبّة من الأحدث للأقدم
    @Query("SELECT * FROM water_entries WHERE userEmail = :email ORDER BY id DESC LIMIT 7")
    suspend fun getLast7Days(email: String): List<WaterEntry>


    // حذف جميع سجلات مستخدم محدد
    @Query("DELETE FROM water_entries WHERE userEmail = :email")
    suspend fun deleteAllByUser(email: String)
}