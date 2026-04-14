// 📁 ui/map/MapScreen.kt
package com.example.luontopeli.ui.map

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.luontopeli.viewmodel.MapViewModel
import com.example.luontopeli.viewmodel.WalkViewModel
import com.example.luontopeli.viewmodel.formatDistance
import com.example.luontopeli.viewmodel.formatDuration
import com.example.luontopeli.viewmodel.toFormattedDate
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    mapViewModel: MapViewModel = hiltViewModel(),
    walkViewModel: WalkViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Suomen oletussijainti (Helsinki), jotta kartta ei aloita Afrikasta
    val defaultPoint = GeoPoint(60.1699, 24.9384)

    // 1. Luodaan MapView vain kerran ja "muistetaan" se
    val mapView = remember { MapView(context) }

    // 2. Hallitaan kartan elinkaarta
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_DESTROY -> mapView.onDetach()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    if (!permissionState.allPermissionsGranted) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Sijaintilupa tarvitaan karttaa varten")
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { permissionState.launchMultiplePermissionRequest() }) {
                    Text("Myönnä lupa")
                }
            }
        }
        return
    }

    val isWalking by walkViewModel.isWalking.collectAsState()
    val routePoints by mapViewModel.routePoints.collectAsState()
    val currentLocation by mapViewModel.currentLocation.collectAsState()
    val natureSpots by mapViewModel.natureSpots.collectAsState()

    // Käynnistetään seuranta aina kun ollaan tällä sivulla
    LaunchedEffect(Unit) {
        mapViewModel.startTracking()
    }

    // Tallennetaan reittipisteet jos kävely on päällä
    LaunchedEffect(currentLocation) {
        currentLocation?.let { loc ->
            if (isWalking) {
                walkViewModel.addRoutePoint(loc.latitude, loc.longitude)
            }
        }
    }

    // Automaattinen keskitys omaan sijaintiin kun sivu aukeaa
    var hasCenteredInitially by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(currentLocation) {
        currentLocation?.let { loc ->
            val userPoint = GeoPoint(loc.latitude, loc.longitude)
            if (!hasCenteredInitially) {
                mapView.controller.setCenter(userPoint)
                hasCenteredInitially = true
            } else if (isWalking) {
                mapView.controller.animateTo(userPoint)
            }
        }
    }

    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
        Configuration.getInstance().load(context, androidx.preference.PreferenceManager.getDefaultSharedPreferences(context))
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            
            val myLocationOverlay = remember {
                MyLocationNewOverlay(GpsMyLocationProvider(context), mapView).apply {
                    enableMyLocation()
                }
            }

            AndroidView(
                factory = {
                    mapView.apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(18.0)
                        controller.setCenter(defaultPoint) // Aloitetaan Suomesta
                        overlays.add(myLocationOverlay)
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { view ->
                    val overlaysToKeep = view.overlays.filter { it is MyLocationNewOverlay }
                    view.overlays.clear()
                    view.overlays.addAll(overlaysToKeep)

                    if (routePoints.size >= 2) {
                        val polyline = Polyline().apply {
                            setPoints(routePoints)
                            outlinePaint.color = 0xFF2E7D32.toInt()
                            outlinePaint.strokeWidth = 12f
                        }
                        view.overlays.add(polyline)
                    }

                    natureSpots.forEach { spot ->
                        val marker = Marker(view).apply {
                            position = GeoPoint(spot.latitude, spot.longitude)
                            title = spot.plantLabel ?: spot.name
                            snippet = spot.timestamp.toFormattedDate()
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        }
                        view.overlays.add(marker)
                    }
                    view.invalidate()
                }
            )
            
            FloatingActionButton(
                onClick = {
                    currentLocation?.let { loc ->
                        mapView.controller.animateTo(GeoPoint(loc.latitude, loc.longitude))
                    }
                },
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Sijainti")
            }
        }

        WalkStatsCard(walkViewModel)
    }
}

@Composable
fun WalkStatsCard(viewModel: WalkViewModel) {
    val session by viewModel.currentSession.collectAsState()
    val isWalking by viewModel.isWalking.collectAsState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (isWalking) "Kävely käynnissä" else "Aloita uusi kävely",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            session?.let { s ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${s.stepCount}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text("askelta", style = MaterialTheme.typography.bodySmall)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = formatDistance(s.distanceMeters),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text("matka", style = MaterialTheme.typography.bodySmall)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = formatDuration(s.startTime),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text("aika", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            ) {
                if (!isWalking) {
                    Button(
                        onClick = { viewModel.startWalk() },
                        modifier = Modifier.weight(1f)
                    ) { Text("Aloita kävely") }
                } else {
                    OutlinedButton(
                        onClick = { viewModel.stopWalk() },
                        modifier = Modifier.weight(1f)
                    ) { Text("Lopeta") }
                }
            }
        }
    }
}
