package com.example.sgp.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.sgp.model.ThreatItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ThreatRepository {

    private val _threatItems = MutableLiveData<MutableList<ThreatItem>>(mutableListOf())
    val threatItems: LiveData<MutableList<ThreatItem>> = _threatItems

    companion object {
        @Volatile
        private var INSTANCE: ThreatRepository? = null

        fun getInstance(): ThreatRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ThreatRepository().also { INSTANCE = it }
            }
        }
    }

    init {
        // Load demo threats initially
        loadDemoThreats()
    }

    suspend fun addThreat(threat: ThreatItem) {
        withContext(Dispatchers.IO) {
            val currentList = _threatItems.value ?: mutableListOf()
            currentList.add(0, threat) // Add to beginning for newest first
            _threatItems.postValue(currentList)
        }
    }

    suspend fun clearAllThreats() {
        withContext(Dispatchers.IO) {
            _threatItems.postValue(mutableListOf())
        }
    }

    suspend fun removeThreat(threat: ThreatItem) {
        withContext(Dispatchers.IO) {
            val currentList = _threatItems.value ?: mutableListOf()
            currentList.remove(threat)
            _threatItems.postValue(currentList)
        }
    }

    fun getThreatCount(): Int {
        return _threatItems.value?.size ?: 0
    }

    private fun loadDemoThreats() {
        val demoThreats = mutableListOf(
            ThreatItem(
                id = 1L,
                messageContent = "🎉 Congratulations! You've won $1,000,000! Click here to claim your prize now! Limited time offer! 💰",
                sender = "+1-555-SCAM",
                sourceApp = "SMS",
                threatType = "PHISHING",
                confidenceScore = 95,
                timestamp = System.currentTimeMillis()
            ),
            ThreatItem(
                id = 2L,
                messageContent = "Your bank account has been suspended. Please verify your identity immediately by clicking this link.",
                sender = "fake-bank@scam.com",
                sourceApp = "WhatsApp",
                threatType = "PHISHING",
                confidenceScore = 89,
                timestamp = System.currentTimeMillis() - 2 * 60 * 60 * 1000 // 2 hours ago
            ),
            ThreatItem(
                id = 3L,
                messageContent = "URGENT: Your account will be deleted in 24 hours. Verify now to prevent loss of data!",
                sender = "security-alert@fake.com",
                sourceApp = "Gmail",
                threatType = "PHISHING",
                confidenceScore = 78,
                timestamp = System.currentTimeMillis() - 24 * 60 * 60 * 1000 // 1 day ago
            )
        )
        _threatItems.postValue(demoThreats)
    }
}
