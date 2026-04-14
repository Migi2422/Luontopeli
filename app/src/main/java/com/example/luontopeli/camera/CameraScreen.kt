package com.example.luontopeli.camera

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.example.luontopeli.ml.ClassificationResult
import com.example.luontopeli.viewmodel.CameraViewModel
import java.io.File

@Composable
fun CameraScreen(
    viewModel: CameraViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Pyydetään sijaintilupa ja käynnistetään seuranta kameran aikana
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasCameraPermission = permissions[Manifest.permission.CAMERA] ?: false
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION
            ))
        }
        // Käynnistetään sijaintiseuranta jotta koordinaatit ovat valmiina tallennusta varten
        viewModel.startLocationTracking()
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopLocationTracking()
        }
    }

    val imageCapture = remember { 
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    val capturedImagePath by viewModel.capturedImagePath.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val classificationResult by viewModel.classificationResult.collectAsState()

    if (!hasCameraPermission) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.CameraAlt, contentDescription = null,
                    modifier = Modifier.size(64.dp), tint = Color.Gray)
                Text("Kameran lupa tarvitaan", modifier = Modifier.padding(8.dp))
                Button(onClick = { 
                    permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION)) 
                }) {
                    Text("Myönnä luvat")
                }
            }
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (capturedImagePath == null) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageCapture
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                FloatingActionButton(
                    onClick = { viewModel.takePhoto(context, imageCapture) },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Icon(Icons.Default.Camera, "Ota kuva", tint = Color.White)
                    }
                }
            }
        } else {
            CapturedImageView(
                imagePath = capturedImagePath!!,
                classificationResult = classificationResult,
                onRetake = { viewModel.clearCapturedImage() },
                onSave = { viewModel.saveCurrentSpot() }
            )
        }
    }
}

@Composable
fun CapturedImageView(
    imagePath: String,
    classificationResult: ClassificationResult?,
    onRetake: () -> Unit,
    onSave: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            AsyncImage(
                model = File(imagePath),
                contentDescription = "Otettu kuva",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            )
            
            classificationResult?.let { result ->
                Box(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
                ) {
                    ClassificationResultCard(result)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedButton(onClick = onRetake) {
                Icon(Icons.Default.Refresh, null)
                Spacer(Modifier.width(8.dp))
                Text("Uudelleen")
            }
            Button(onClick = onSave) {
                Icon(Icons.Default.Save, null)
                Spacer(Modifier.width(8.dp))
                Text("Tallenna")
            }
        }
    }
}

@Composable
fun ClassificationResultCard(result: ClassificationResult) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (result) {
                is ClassificationResult.Success ->
                    if (result.confidence > 0.8f) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            when (result) {
                is ClassificationResult.Success -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Tunnistettu:",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(Modifier.width(8.dp))
                        SuggestionChip(
                            onClick = { },
                            label = { Text("${"%.0f".format(result.confidence * 100)}%") }
                        )
                    }

                    Text(
                        text = result.label,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    LinearProgressIndicator(
                        progress = { result.confidence },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                    )
                }
                is ClassificationResult.NotNature -> {
                    Text("Ei luontokohde", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Kuvassa näkyy: ${result.allLabels.joinToString { it.text }}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                is ClassificationResult.Error -> {
                    Text("Virhe: ${result.message}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
