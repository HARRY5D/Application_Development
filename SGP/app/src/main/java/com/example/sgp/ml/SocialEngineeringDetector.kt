package com.example.sgp.ml

import android.content.Context
import android.util.Log

/**
 * A simplified detector for social engineering attacks in messages.
 * This implementation doesn't require a real TensorFlow Lite model for demonstration purposes.
 */
class SocialEngineeringDetector(private val context: Context) {

    private val TAG = "SEDetector"
    private val suspiciousTerms = listOf(
        "password", "bank", "account", "verify", "urgent", "click",
        "link", "money", "transfer", "win", "credit card", "offer",
        "free", "limited time", "social security", "confirm", "update",
        "login", "secure", "suspicious", "access"
    )

    fun detectPhishing(message: String): PhishingResult {
        // Simple keyword-based approach for demo purposes
        val messageText = message.lowercase()
        var confidence = 0.0f
        var suspiciousTermsFound = 0

        // Count suspicious terms in the message
        for (term in suspiciousTerms) {
            if (messageText.contains(term)) {
                suspiciousTermsFound++
                Log.d(TAG, "Found suspicious term: $term in message")
                confidence += 0.15f  // Increase confidence for each term found
            }
        }

        // Check for URLs
        if (messageText.contains("http") || messageText.contains("www")) {
            confidence += 0.3f
            Log.d(TAG, "Found URL in message")
        }

        // Normalize confidence to 0-1 range
        confidence = confidence.coerceAtMost(1.0f)

        // Determine if the message is likely phishing
        val isPhishing = confidence > 0.4f

        Log.d(TAG, "Message phishing detection - Result: $isPhishing, Confidence: $confidence")
        return PhishingResult(isPhishing, confidence)
    }

    data class PhishingResult(val isPhishing: Boolean, val confidence: Float)
}
