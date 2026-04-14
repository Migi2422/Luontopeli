package com.example.luontopeli.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luontopeli.data.repository.NatureSpotRepository
import com.example.luontopeli.data.local.entity.NatureSpot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val repository: NatureSpotRepository
) : ViewModel() {
    val allSpots: StateFlow<List<NatureSpot>> = repository.getAllSpots()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val discoveredSpots: StateFlow<List<NatureSpot>> = repository.getDiscoveredSpots()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

