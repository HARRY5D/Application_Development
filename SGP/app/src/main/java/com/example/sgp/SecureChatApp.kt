package com.example.sgp

import android.app.Application
import android.util.Log

class SecureChatApp : Application() {

    companion object {
        private const val TAG = "SecureChatApp"
    }

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "Initializing Secure Chat Lite")

        // In a real app, we would initialize our ML model here
        // SEDetectionModel.getInstance(applicationContext)

        // For demo purposes, we're not initializing any real services or APIs
    }
}
