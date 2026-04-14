package com.example.luontopeli.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.osmdroid.util.GeoPoint

class LocationManager(context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    private val _routePoints = MutableStateFlow<List<GeoPoint>>(emptyList())
    val routePoints: StateFlow<List<GeoPoint>> = _routePoints.asStateFlow()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                _currentLocation.value = location
                val newPoint = GeoPoint(location.latitude, location.longitude)
                _routePoints.value = _routePoints.value + newPoint
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startTracking() {
        // Haetaan viimeisin tunnettu sijainti heti alkuun, jotta kartta ei näytä nollaa
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                if (_currentLocation.value == null) {
                    _currentLocation.value = it
                }
            }
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
            .setMinUpdateDistanceMeters(5f)
            .build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    fun stopTracking() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    fun resetRoute() {
        _routePoints.value = emptyList()
    }
}
