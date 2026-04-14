package com.example.luontopeli.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "walk_sessions")
data class WalkSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val distanceMeters: Float = 0f,
    val stepCount: Int = 0,
    val isActive: Boolean = true
)
