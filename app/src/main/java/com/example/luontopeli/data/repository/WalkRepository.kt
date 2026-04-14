package com.example.luontopeli.data.repository

import com.example.luontopeli.data.local.dao.RoutePointDao
import com.example.luontopeli.data.local.dao.WalkSessionDao
import com.example.luontopeli.data.local.entity.RoutePoint
import com.example.luontopeli.data.local.entity.WalkSession
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalkRepository @Inject constructor(
    private val walkSessionDao: WalkSessionDao,
    private val routePointDao: RoutePointDao
) {
    fun getAllSessions(): Flow<List<WalkSession>> = walkSessionDao.getAllSessions()

    suspend fun startSession(startTime: Long): Long {
        val session = WalkSession(startTime = startTime)
        return walkSessionDao.insertSession(session)
    }

    suspend fun updateSession(session: WalkSession) {
        walkSessionDao.updateSession(session)
    }

    suspend fun getSessionById(id: Long): WalkSession? {
        return walkSessionDao.getSessionById(id)
    }

    suspend fun insertRoutePoint(point: RoutePoint) {
        routePointDao.insert(point)
    }

    fun getPointsForSession(sessionId: Long): Flow<List<RoutePoint>> {
        return routePointDao.getPointsForSession(sessionId)
    }
}
