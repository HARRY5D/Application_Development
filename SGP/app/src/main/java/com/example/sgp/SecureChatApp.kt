package com.example.sgp

import android.app.Application
import android.util.Log
import com.example.sgp.data.SecureChatDatabase
import com.example.sgp.encryption.EncryptionManager
import com.example.sgp.ml.ModelManager
import com.example.sgp.repository.MessageRepository
import com.example.sgp.repository.ThreatRepository
import com.example.sgp.repository.UserRepository

class SecureChatApp : Application() {

    companion object {
        private const val TAG = "SecureChatApp"
    }

    // Database instance
    val database by lazy { SecureChatDatabase.getInstance(this) }

    // Repositories
    val messageRepository by lazy { MessageRepository(database) }
    val userRepository by lazy { UserRepository(database) }
    val threatRepository by lazy { ThreatRepository.getInstance() }

    // Security and ML components
    val encryptionManager by lazy { EncryptionManager(this) }
    val modelManager by lazy { ModelManager(this) }

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "Initializing Secure Chat Lite")

        // Initialize security components
        initializeSecurity()

        // Initialize ML models
        initializeML()

        // Initialize threat repository
        initializeThreatRepository()

        Log.d(TAG, "Secure Chat Lite initialization complete")
    }

    private fun initializeSecurity() {
        // Encryption manager is initialized in its constructor
        // Additional security setup can be added here
        Log.d(TAG, "Security components initialized")
    }

    private fun initializeML() {
        // Model manager will initialize the ML models asynchronously
        // No blocking operations needed here
        Log.d(TAG, "ML components initialization started")
    }

    private fun initializeThreatRepository() {
        // Initialize threat repository to ensure it's ready when services need it
        threatRepository
        Log.d(TAG, "Threat repository initialized")
    }
}
