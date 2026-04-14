package com.example.luontopeli.ui.stats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.luontopeli.data.local.entity.WalkSession
import com.example.luontopeli.viewmodel.StatsViewModel
import com.example.luontopeli.viewmodel.formatDistance
import com.example.luontopeli.viewmodel.formatDuration
import com.example.luontopeli.viewmodel.toFormattedDate

@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel()
) {
    val totalDistance by viewModel.totalDistance.collectAsState()
    val spotCount by viewModel.discoveredSpotsCount.collectAsState()
    val history by viewModel.walkHistory.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Tilastosi",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Walk Card
            StatCard(
                modifier = Modifier.weight(1f),
                label = "Kävelty yhteensä",
                value = formatDistance(totalDistance),
                icon = Icons.Default.DirectionsWalk,
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )

            // Discover Card
            StatCard(
                modifier = Modifier.weight(1f),
                label = "Löydetty",
                value = "$spotCount kohdetta",
                icon = Icons.Default.LocationOn,
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.History, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Kävelyhistoria", style = MaterialTheme.typography.titleLarge)
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        if (history.isEmpty()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(text = "Ei vielä tallennettuja kävelyjä.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(history.filter { !it.isActive }) { session ->
                    HistoryItem(session)
                }
            }
        }
    }
}

@Composable
fun HistoryItem(session: WalkSession) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = session.startTime.toFormattedDate(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = formatDuration(session.startTime, session.endTime ?: System.currentTimeMillis()),
                    style = MaterialTheme.typography.labelMedium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Matka", style = MaterialTheme.typography.labelSmall)
                    Text(text = formatDistance(session.distanceMeters), style = MaterialTheme.typography.bodyLarge)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Askeleet", style = MaterialTheme.typography.labelSmall)
                    Text(text = "${session.stepCount}", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    icon: ImageVector,
    containerColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = label, style = MaterialTheme.typography.labelMedium)
            Text(text = value, style = MaterialTheme.typography.titleLarge)
        }
    }
}
