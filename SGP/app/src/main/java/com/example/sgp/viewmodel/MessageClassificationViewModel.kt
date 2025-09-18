package com.example.sgp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.sgp.ml.MessageClassifier
import com.example.sgp.ml.ThreatDetector
import kotlinx.coroutines.launch

class MessageClassificationViewModel(application: Application) : AndroidViewModel(application) {

    private val messageClassifier = MessageClassifier(application)
    private val threatDetector = ThreatDetector()

    private val _classificationResult = MutableLiveData<MessageClassifier.ClassificationResult>()
    val classificationResult: LiveData<MessageClassifier.ClassificationResult> = _classificationResult

    private val _threatStats = MutableLiveData<ThreatStats>()
    val threatStats: LiveData<ThreatStats> = _threatStats

    private val _isProcessing = MutableLiveData<Boolean>()
    val isProcessing: LiveData<Boolean> = _isProcessing

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    init {
        loadThreatStats()
    }

    fun classifyMessage(message: String) {
        viewModelScope.launch {
            try {
                _isProcessing.value = true

                val result = messageClassifier.classifyMessage(message)
                _classificationResult.value = result

                // Update threat statistics
                updateThreatStats(result)

                _isProcessing.value = false
            } catch (e: Exception) {
                _error.value = "Classification failed: ${e.message}"
                _isProcessing.value = false
            }
        }
    }

    fun batchClassifyMessages(messages: List<String>) {
        viewModelScope.launch {
            try {
                _isProcessing.value = true

                val results = messages.map { message ->
                    messageClassifier.classifyMessage(message)
                }

                // Process batch results
                val threatCount = results.count { it.isThreat }
                val totalCount = results.size

                _threatStats.value = _threatStats.value?.copy(
                    totalMessages = _threatStats.value!!.totalMessages + totalCount,
                    threatsDetected = _threatStats.value!!.threatsDetected + threatCount
                )

                _isProcessing.value = false
            } catch (e: Exception) {
                _error.value = "Batch classification failed: ${e.message}"
                _isProcessing.value = false
            }
        }
    }

    fun analyzeMessagePatterns(messages: List<String>) {
        viewModelScope.launch {
            try {
                val analysis = threatDetector.analyzePatterns(messages)
                // Handle pattern analysis results
            } catch (e: Exception) {
                _error.value = "Pattern analysis failed: ${e.message}"
            }
        }
    }

    fun resetStats() {
        _threatStats.value = ThreatStats()
    }

    private fun loadThreatStats() {
        _threatStats.value = ThreatStats(
            totalMessages = 0,
            threatsDetected = 0,
            phishingAttempts = 0,
            spamMessages = 0,
            abusiveContent = 0
        )
    }

    private fun updateThreatStats(result: MessageClassifier.ClassificationResult) {
        val currentStats = _threatStats.value ?: return

        val updatedStats = currentStats.copy(
            totalMessages = currentStats.totalMessages + 1,
            threatsDetected = if (result.isThreat) currentStats.threatsDetected + 1 else currentStats.threatsDetected,
            phishingAttempts = if (result.threatType == "phishing") currentStats.phishingAttempts + 1 else currentStats.phishingAttempts,
            spamMessages = if (result.threatType == "spam") currentStats.spamMessages + 1 else currentStats.spamMessages,
            abusiveContent = if (result.threatType == "abusive") currentStats.abusiveContent + 1 else currentStats.abusiveContent
        )

        _threatStats.value = updatedStats
    }

    data class ThreatStats(
        val totalMessages: Int = 0,
        val threatsDetected: Int = 0,
        val phishingAttempts: Int = 0,
        val spamMessages: Int = 0,
        val abusiveContent: Int = 0
    )
}
