package com.example.luontopeli.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.luontopeli.data.local.entity.RoutePoint
import com.example.luontopeli.data.local.entity.WalkSession
import com.example.luontopeli.data.repository.WalkRepository
import com.example.luontopeli.sensor.StepCounterManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalkViewModel @Inject constructor(
    application: Application,
    private val repository: WalkRepository
) : AndroidViewModel(application) {

    private val stepManager = StepCounterManager(application)

    private val _currentSession = MutableStateFlow<WalkSession?>(null)
    val currentSession: StateFlow<WalkSession?> = _currentSession.asStateFlow()

    private val _isWalking = MutableStateFlow(false)
    val isWalking: StateFlow<Boolean> = _isWalking.asStateFlow()

    private var currentSessionId: Long = -1

    fun startWalk() {
        if (_isWalking.value) return

        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            currentSessionId = repository.startSession(startTime)
            
            _currentSession.value = WalkSession(
                id = currentSessionId,
                startTime = startTime
            )
            _isWalking.value = true

            stepManager.startStepCounting {
                _currentSession.update { current ->
                    current?.copy(
                        stepCount = current.stepCount + 1,
                        distanceMeters = current.distanceMeters + 0.74f
                    )
                }
            }
        }
    }

    fun addRoutePoint(lat: Double, lon: Double) {
        if (currentSessionId != -1L && _isWalking.value) {
            viewModelScope.launch {
                repository.insertRoutePoint(
                    RoutePoint(
                        sessionId = currentSessionId,
                        latitude = lat,
                        longitude = lon
                    )
                )
            }
        }
    }

    fun stopWalk() {
        stepManager.stopStepCounting()
        _isWalking.value = false
        
        val finishedSession = _currentSession.value?.copy(
            endTime = System.currentTimeMillis(),
            isActive = false
        )

        viewModelScope.launch {
            finishedSession?.let {
                repository.updateSession(it)
            }
            currentSessionId = -1
        }
    }

    override fun onCleared() {
        super.onCleared()
        stepManager.stopAll()
    }
}

fun formatDistance(meters: Float): String {
    return if (meters < 1000f) {
        "${meters.toInt()} m"
    } else {
        "%.1f km".format(meters / 1000f)
    }
}

fun formatDuration(startTime: Long, endTime: Long = System.currentTimeMillis()): String {
    val seconds = (endTime - startTime) / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    return when {
        hours > 0 -> "${hours}h ${minutes % 60}min"
        minutes > 0 -> "${minutes}min ${seconds % 60}s"
        else -> "${seconds}s"
    }
}
