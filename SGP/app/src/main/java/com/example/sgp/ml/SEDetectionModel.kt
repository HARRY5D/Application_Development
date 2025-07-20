package com.example.sgp.ml

import android.content.Context
import android.util.Log
import java.util.regex.Pattern

class SEDetectionModel private constructor(private val context: Context) {

    private val modelName = "se_detection_model.tflite"
    private val inputSize = 256 // Adjust based on actual model requirements
    private val labels = arrayOf("SAFE", "SMISHING", "PHISHING")

    // Suspicious URL patterns
    private val URL_PATTERN = Pattern.compile(
        "\\b(?:https?://|www\\.)[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]",
        Pattern.CASE_INSENSITIVE
    )

    // Suspicious keywords for phishing/smishing detection
    private val SUSPICIOUS_KEYWORDS = arrayOf(
        "click here", "urgent", "verify account", "suspended", "confirm identity",
        "limited time", "act now", "congratulations", "winner", "free gift",
        "claim now", "verify now", "update payment", "security alert",
        "account locked", "immediate action", "expire today", "prize",
        "lottery", "bitcoin", "cryptocurrency", "investment opportunity"
    )

    companion object {
        @Volatile
        private var INSTANCE: SEDetectionModel? = null
        private const val TAG = "SEDetectionModel"

        fun getInstance(context: Context): SEDetectionModel {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SEDetectionModel(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    init {
        // Mock initialization - in real implementation, this would load the TFLite model
        Log.d(TAG, "SEDetectionModel initialized (mock version)")
    }

    /**
     * Mock prediction function for demo purposes
     * In real implementation, this would use TensorFlow Lite inference
     */
    fun predictSEAttack(message: String): SEResult {
        return try {
            // Mock prediction logic for demo
            val messageLC = message.lowercase()

            // Check for suspicious URLs
            val hasURL = URL_PATTERN.matcher(message).find()

            // Check for suspicious keywords
            val suspiciousKeywordCount = SUSPICIOUS_KEYWORDS.count { keyword ->
                messageLC.contains(keyword.lowercase())
            }

            // Mock prediction based on simple rules
            val prediction = when {
                suspiciousKeywordCount >= 2 -> "PHISHING"
                hasURL && suspiciousKeywordCount >= 1 -> "SMISHING"
                messageLC.contains("click") && hasURL -> "SMISHING"
                messageLC.contains("urgent") && messageLC.contains("verify") -> "PHISHING"
                else -> "SAFE"
            }

            val confidence = when (prediction) {
                "PHISHING" -> 0.85f + (suspiciousKeywordCount * 0.05f)
                "SMISHING" -> 0.75f + (if (hasURL) 0.1f else 0.0f)
                else -> 0.95f
            }.coerceAtMost(0.99f)

            Log.d(TAG, "Mock prediction: $prediction with confidence: $confidence")

            SEResult(
                prediction = prediction,
                confidence = confidence,
                threatLevel = getThreatLevel(prediction, confidence),
                detectedPatterns = getDetectedPatterns(message, hasURL, suspiciousKeywordCount)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error in mock prediction", e)
            SEResult("SAFE", 0.5f, ThreatLevel.LOW, emptyList())
        }
    }

    private fun getThreatLevel(prediction: String, confidence: Float): ThreatLevel {
        return when {
            prediction == "SAFE" -> ThreatLevel.LOW
            confidence > 0.8f -> ThreatLevel.HIGH
            confidence > 0.6f -> ThreatLevel.MEDIUM
            else -> ThreatLevel.LOW
        }
    }

    private fun getDetectedPatterns(message: String, hasURL: Boolean, suspiciousKeywordCount: Int): List<String> {
        val patterns = mutableListOf<String>()

        if (hasURL) {
            patterns.add("Suspicious URL detected")
        }

        if (suspiciousKeywordCount > 0) {
            patterns.add("$suspiciousKeywordCount suspicious keywords found")
        }

        val messageLC = message.lowercase()
        if (messageLC.contains("urgent") || messageLC.contains("immediate")) {
            patterns.add("Urgency tactics detected")
        }

        if (messageLC.contains("verify") || messageLC.contains("confirm")) {
            patterns.add("Verification request detected")
        }

        return patterns
    }

    /**
     * Check if the model is ready (always true for mock)
     */
    fun isModelReady(): Boolean {
        return true // Mock implementation - always ready
    }

    /**
     * Get model info
     */
    fun getModelInfo(): String {
        return "Mock SE Detection Model v1.0 (Demo Version)"
    }

    /**
     * Cleanup resources
     */
    fun close() {
        // Mock cleanup - in real implementation would close TFLite interpreter
        Log.d(TAG, "SEDetectionModel closed (mock version)")
    }
}

/**
 * Result class for SE detection
 */
data class SEResult(
    val prediction: String,
    val confidence: Float,
    val threatLevel: ThreatLevel,
    val detectedPatterns: List<String>
)

/**
 * Threat level enum
 */
enum class ThreatLevel {
    LOW, MEDIUM, HIGH
}
