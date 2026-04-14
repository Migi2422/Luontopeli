package com.example.luontopeli.data.remote.firebase

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthManager @Inject constructor() {
    // Stubbed user info
    val currentUserEmail: String? = "testuser@example.com"
    val currentUserId: String? = "stub_user_id"

    fun isUserLoggedIn(): Boolean = true

    fun signOut() {
        // Do nothing in stub
    }
}
