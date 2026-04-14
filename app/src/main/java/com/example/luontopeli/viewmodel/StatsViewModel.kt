package com.example.luontopeli.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luontopeli.data.local.entity.WalkSession
import com.example.luontopeli.data.repository.NatureSpotRepository
import com.example.luontopeli.data.repository.WalkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: WalkRepository,
    private val natureSpotRepository: NatureSpotRepository
) : ViewModel() {

    val walkHistory: StateFlow<List<WalkSession>> = repository.getAllSessions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val totalDistance: StateFlow<Float> = walkHistory
        .map { sessions ->
            sessions.sumOf { it.distanceMeters.toDouble() }.toFloat()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0f
        )

    val discoveredSpotsCount: StateFlow<Int> = natureSpotRepository.getDiscoveredSpots()
        .map { spots -> spots.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
}
