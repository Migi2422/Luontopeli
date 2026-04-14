package com.example.luontopeli.data.remote.firebase

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageManager @Inject constructor() {
    // Stubbed Storage operations
    suspend fun uploadImage(bytes: ByteArray): String? {
        // Return a local mock URL or null for now
        return null
    }
}
