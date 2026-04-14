package com.example.luontopeli.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    // Firebase providers removed for offline stubbing.
    // Managers (AuthManager, etc.) are now providing their own stubs via @Inject constructor().
}
