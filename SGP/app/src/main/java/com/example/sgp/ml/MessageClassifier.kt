package com.example.sgp.ml

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class MessageClassifier(private val context: Context) {

    private var interpreter: Interpreter? = null
    private val maxSequenceLength = 128
    private val vocabSize = 30522 // TinyBERT vocab size
    private var isModelLoaded = false

    companion object {
        private const val TAG = "MessageClassifier"
        private const val MODEL_PATH = "models/tinybert_quantized.tflite"
    }

    init {
        loadModel()
    }

    private fun loadModel() {
        try {
            Log.d(TAG, "Loading TensorFlow Lite model...")
            val modelBuffer = loadModelFile()
            val options = Interpreter.Options().apply {
                setNumThreads(4)
                setUseXNNPACK(true)
            }
            interpreter = Interpreter(modelBuffer, options)
            isModelLoaded = true
            Log.i(TAG, "TensorFlow Lite model loaded successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load TensorFlow Lite model, falling back to rule-based detection", e)
            isModelLoaded = false
        }
    }

    private fun loadModelFile(): MappedByteBuffer {
        val assetFileDescriptor = context.assets.openFd(MODEL_PATH)
        val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun classifyMessage(message: String): ClassificationResult {
        return try {
            if (isModelLoaded && interpreter != null) {
                Log.d(TAG, "Using ML model for classification")
                val preprocessedInput = preprocessMessage(message)
                val output = runInference(preprocessedInput)
                val result = parseOutput(output, message)

                // If ML model gives low confidence, fall back to rule-based
                if (result.confidence < 0.3f) {
                    Log.d(TAG, "ML confidence too low (${result.confidence}), using rule-based detection")
                    performEnhancedRuleBasedDetection(message)
                } else {
                    result
                }
            } else {
                Log.d(TAG, "Using rule-based detection (ML model not available)")
                performEnhancedRuleBasedDetection(message)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Classification failed, using rule-based fallback", e)
            performEnhancedRuleBasedDetection(message)
        }
    }

    private fun performEnhancedRuleBasedDetection(message: String): ClassificationResult {
        val lowerMessage = message.lowercase()

        // Enhanced phishing detection patterns
        val phishingPatterns = listOf(
            // Account security threats
            "account.*suspended", "account.*locked", "account.*compromised",
            "verify.*account", "confirm.*identity", "update.*payment",
            "security.*alert", "unauthorized.*access", "suspicious.*activity",

            // Urgency tactics
            "urgent.*action.*required", "immediate.*action", "expires.*today",
            "limited.*time", "act.*now", "within.*24.*hours",

            // Prize/reward scams
            "congratulations.*won", "claim.*prize", "claim.*reward",
            "winner.*selected", "lottery.*winner", "cash.*prize",

            // Financial threats
            "tax.*refund", "irs.*notice", "bank.*notice", "payment.*failed",
            "credit.*card.*expired", "billing.*issue", "overdraft.*notice",

            // Links and calls to action
            "click.*here.*immediately", "verify.*now", "update.*now",
            "download.*attachment", "open.*link", "visit.*website"
        )

        val spamPatterns = listOf(
            // Marketing spam
            "make.*money.*fast", "work.*from.*home", "earn.*\\$.*daily",
            "free.*gift", "free.*trial", "no.*cost", "risk.*free",
            "buy.*now", "limited.*offer", "special.*deal", "discount.*expires",
            "call.*now", "order.*today", "don't.*miss.*out",

            // Investment scams
            "guaranteed.*profit", "investment.*opportunity", "double.*your.*money",
            "crypto.*investment", "forex.*trading", "stock.*tips",

            // Dating/romance scams
            "lonely.*heart", "find.*love", "dating.*site", "meet.*singles",

            // Health/medicine spam
            "lose.*weight.*fast", "miracle.*cure", "natural.*supplement",
            "viagra", "cialis", "diet.*pills"
        )

        val abusivePatterns = listOf(
            // Explicit threats
            "i.*will.*kill", "going.*to.*hurt", "watch.*your.*back",
            "you.*will.*die", "i.*know.*where.*you.*live",

            // Harassment
            "stupid.*idiot", "go.*kill.*yourself", "worthless.*piece",
            "nobody.*likes.*you", "you.*should.*die",

            // Hate speech indicators
            "i.*hate.*you", "disgusting.*person", "piece.*of.*trash"
        )

        // Check for phishing
        val phishingMatches = phishingPatterns.count { pattern =>
            lowerMessage.contains(Regex(pattern))
        }

        val spamMatches = spamPatterns.count { pattern =>
            lowerMessage.contains(Regex(pattern))
        }

        val abusiveMatches = abusivePatterns.count { pattern =>
            lowerMessage.contains(Regex(pattern))
        }

        // Determine threat type and confidence based on matches
        return when {
            phishingMatches > 0 -> {
                val confidence = minOf(0.7f + (phishingMatches * 0.1f), 0.95f)
                ClassificationResult(
                    true, "phishing", confidence,
                    "Detected ${phishingMatches} phishing indicator(s) - Rule-based detection"
                )
            }
            spamMatches > 0 -> {
                val confidence = minOf(0.65f + (spamMatches * 0.1f), 0.9f)
                ClassificationResult(
                    true, "spam", confidence,
                    "Detected ${spamMatches} spam indicator(s) - Rule-based detection"
                )
            }
            abusiveMatches > 0 -> {
                val confidence = minOf(0.75f + (abusiveMatches * 0.1f), 0.95f)
                ClassificationResult(
                    true, "abusive", confidence,
                    "Detected ${abusiveMatches} abusive indicator(s) - Rule-based detection"
                )
            }
            else -> {
                ClassificationResult(
                    false, "safe", 0.9f,
                    "No threat patterns detected - Rule-based analysis"
                )
            }
        }
    }

    private fun preprocessMessage(message: String): Array<FloatArray> {
        // Simple tokenization and padding
        val tokens = tokenizeMessage(message)
        val paddedTokens = padTokens(tokens)

        // Convert to input format expected by TinyBERT
        return arrayOf(paddedTokens.map { it.toFloat() }.toFloatArray())
    }

    private fun tokenizeMessage(message: String): IntArray {
        // Simple tokenization - in production, use proper BERT tokenizer
        val words = message.lowercase().split("\\s+".toRegex())
        return words.take(maxSequenceLength).map { word ->
            // Simple hash-based token mapping
            Math.abs(word.hashCode()) % vocabSize
        }.toIntArray()
    }

    private fun padTokens(tokens: IntArray): IntArray {
        return if (tokens.size >= maxSequenceLength) {
            tokens.take(maxSequenceLength).toIntArray()
        } else {
            tokens + IntArray(maxSequenceLength - tokens.size) { 0 }
        }
    }

    private fun runInference(input: Array<FloatArray>): FloatArray {
        val output = Array(1) { FloatArray(4) } // 4 classes: safe, spam, phishing, abusive

        interpreter?.run(input, output)

        return output[0]
    }

    private fun parseOutput(output: FloatArray, originalMessage: String): ClassificationResult {
        val classes = arrayOf("safe", "spam", "phishing", "abusive")
        val maxIndex = output.indices.maxByOrNull { output[it] } ?: 0
        val confidence = output[maxIndex]

        val isThreat = maxIndex > 0 && confidence > 0.7f
        val threatType = if (isThreat) classes[maxIndex] else "safe"

        return ClassificationResult(
            isThreat = isThreat,
            threatType = threatType,
            confidence = confidence,
            explanation = generateExplanation(threatType, confidence, originalMessage)
        )
    }

    private fun generateExplanation(threatType: String, confidence: Float, message: String): String {
        return when (threatType) {
            "spam" -> "This message appears to be spam (${(confidence * 100).toInt()}% confidence) - ML model"
            "phishing" -> "This message may be a phishing attempt (${(confidence * 100).toInt()}% confidence) - ML model"
            "abusive" -> "This message contains potentially abusive content (${(confidence * 100).toInt()}% confidence) - ML model"
            else -> "This message appears safe (${(confidence * 100).toInt()}% confidence) - ML model"
        }
    }

    fun close() {
        interpreter?.close()
    }

    data class ClassificationResult(
        val isThreat: Boolean,
        val threatType: String,
        val confidence: Float,
        val explanation: String
    )
}
