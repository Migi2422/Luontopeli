package com.example.luontopeli.viewmodel

import android.content.Context
import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luontopeli.data.local.entity.NatureSpot
import com.example.luontopeli.data.repository.NatureSpotRepository
import com.example.luontopeli.location.LocationManager
import com.example.luontopeli.ml.ClassificationResult
import com.example.luontopeli.ml.PlantClassifier
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val repository: NatureSpotRepository,
    private val locationManager: LocationManager
) : ViewModel() {

    private val classifier = PlantClassifier()

    private val _capturedImagePath = MutableStateFlow<String?>(null)
    val capturedImagePath: StateFlow<String?> = _capturedImagePath.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _classificationResult = MutableStateFlow<ClassificationResult?>(null)
    val classificationResult: StateFlow<ClassificationResult?> = _classificationResult.asStateFlow()

    fun startLocationTracking() = locationManager.startTracking()
    fun stopLocationTracking() = locationManager.stopTracking()

    fun takePhoto(context: Context, imageCapture: ImageCapture) {
        _isLoading.value = true
        viewModelScope.launch {
            val imagePath = takePhotoSuspend(context, imageCapture)
            if (imagePath == null) {
                _isLoading.value = false
                return@launch
            }

            _capturedImagePath.value = imagePath

            try {
                val uri = Uri.fromFile(File(imagePath))
                val result = classifier.classify(uri, context)
                _classificationResult.value = result
            } catch (e: Exception) {
                _classificationResult.value = ClassificationResult.Error(e.message ?: "Tuntematon virhe")
            }

            _isLoading.value = false
        }
    }

    private suspend fun takePhotoSuspend(context: Context, imageCapture: ImageCapture): String? = suspendCoroutine { continuation ->
        val photoFile = File(
            context.externalCacheDir,
            "photo_${System.currentTimeMillis()}.jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            androidx.core.content.ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    continuation.resume(photoFile.absolutePath)
                }

                override fun onError(exception: ImageCaptureException) {
                    continuation.resume(null)
                }
            }
        )
    }

    fun saveCurrentSpot() {
        val imagePath = _capturedImagePath.value ?: return
        val location = locationManager.currentLocation.value
        
        viewModelScope.launch {
            val result = _classificationResult.value

            val spot = NatureSpot(
                name = when (result) {
                    is ClassificationResult.Success -> result.label
                    else -> "Luontolöytö"
                },
                latitude = location?.latitude ?: 0.0,
                longitude = location?.longitude ?: 0.0,
                imageLocalPath = imagePath,
                plantLabel = (result as? ClassificationResult.Success)?.label,
                confidence = (result as? ClassificationResult.Success)?.confidence,
                isDiscovered = true
            )
            repository.insertSpot(spot)
            clearCapturedImage()
        }
    }

    fun clearCapturedImage() {
        _capturedImagePath.value = null
        _classificationResult.value = null
    }

    override fun onCleared() {
        super.onCleared()
        classifier.close()
    }
}
