package com.lecturevault.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "materials")
data class Material(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseId: Long,
    val type: String,       // "photo", "pdf", "audio"
    val rawText: String,
    val createdAt: Long = System.currentTimeMillis()
)
