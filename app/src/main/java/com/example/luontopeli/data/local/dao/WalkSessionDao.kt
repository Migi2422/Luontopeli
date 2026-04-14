package com.example.luontopeli.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.luontopeli.data.local.entity.WalkSession
import kotlinx.coroutines.flow.Flow

@Dao
interface WalkSessionDao {
    @Query("SELECT * FROM walk_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<WalkSession>>

    @Insert
    suspend fun insertSession(session: WalkSession): Long

    @Update
    suspend fun updateSession(session: WalkSession)

    @Query("SELECT * FROM walk_sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): WalkSession?
}
