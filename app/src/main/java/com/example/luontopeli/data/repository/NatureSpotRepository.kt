package com.example.luontopeli.data.repository

import com.example.luontopeli.data.local.dao.NatureSpotDao
import com.example.luontopeli.data.local.entity.NatureSpot
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NatureSpotRepository @Inject constructor(
    private val natureSpotDao: NatureSpotDao
) {
    fun getAllSpots(): Flow<List<NatureSpot>> = natureSpotDao.getAllSpots()

    fun getDiscoveredSpots(): Flow<List<NatureSpot>> = natureSpotDao.getDiscoveredSpots()

    fun getSpotsWithLocation(): Flow<List<NatureSpot>> = natureSpotDao.getSpotsWithLocation()

    suspend fun insertSpot(spot: NatureSpot) {
        natureSpotDao.insert(spot)
    }

    suspend fun deleteSpot(spot: NatureSpot) {
        natureSpotDao.delete(spot)
    }

    suspend fun getUnsyncedSpots(): List<NatureSpot> {
        return natureSpotDao.getUnsyncedSpots()
    }

    suspend fun markSynced(id: String, url: String) {
        natureSpotDao.markSynced(id, url)
    }
}
