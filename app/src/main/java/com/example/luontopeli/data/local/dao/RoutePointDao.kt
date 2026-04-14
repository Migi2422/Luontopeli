package com.example.luontopeli.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.luontopeli.data.local.entity.RoutePoint
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutePointDao {
    @Insert
    suspend fun insert(point: RoutePoint)

    @Query("SELECT * FROM route_points WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getPointsForSession(sessionId: Long): Flow<List<RoutePoint>>

    @Query("DELETE FROM route_points WHERE sessionId = :sessionId")
    suspend fun deletePointsForSession(sessionId: Long)
}
