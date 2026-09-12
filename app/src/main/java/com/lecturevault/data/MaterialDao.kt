package com.lecturevault.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MaterialDao {
    @Insert
    suspend fun insert(material: Material): Long

    @Query("SELECT * FROM materials ORDER BY createdAt DESC")
    fun getAll(): Flow<List<Material>>

    @Query("SELECT * FROM materials WHERE courseId = :courseId ORDER BY createdAt DESC")
    fun getByCourse(courseId: Long): Flow<List<Material>>
}
