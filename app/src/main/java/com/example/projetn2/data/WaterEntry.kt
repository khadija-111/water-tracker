package com.example.projetn2.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "water_entries")
data class WaterEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // مفتاح فريد لكل إدخال
    val userEmail: String,                             // البريد ديال المستخدم
    val dayIndex: Int,                                 // اليوم (0 = lundi, ... 6 = dimanche)
    val amount: Float                                  // كمية الماء باللتر
)