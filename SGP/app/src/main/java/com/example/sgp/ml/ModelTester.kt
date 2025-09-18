package com.example.sgp.ml

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ModelTester(private val context: Context) {

    private val TAG = "ModelTester"

    fun runBasicTests() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Starting TensorFlow Lite model tests...")

                // Test 1: Model Loading
                testModelLoading()

                // Test 2: Basic Classification
                testBasicClassification()

                // Test 3: Threat Detection Patterns
                testThreatDetectionPatterns()

                // Test 4: Model Manager Integration
                testModelManagerIntegration()

                Log.d(TAG, "All model tests completed successfully!")

            } catch (e: Exception) {
                Log.e(TAG, "Model testing failed: ${e.message}", e)
            }
        }
    }

    private suspend fun testModelLoading() {
        withContext(Dispatchers.IO) {
            Log.d(TAG, "Testing model loading...")

            val classifier = MessageClassifier(context)
            val testResult = classifier.classifyMessage("This is a test message")

            if (testResult.explanation.isNotEmpty()) {
                Log.d(TAG, "✅ Model loading test passed")
                Log.d(TAG, "Test result: ${testResult.explanation}")
            } else {
                Log.w(TAG, "⚠️ Model loading test completed but no explanation returned")
            }
        }
    }

    private suspend fun testBasicClassification() {
        withContext(Dispatchers.IO) {
            Log.d(TAG, "Testing basic message classification...")

            val classifier = MessageClassifier(context)
            val testMessages = listOf(
                "Hello, how are you today?", // Safe message
                "URGENT: Click here to verify your account!", // Phishing
                "FREE MONEY! Win $1000 now!", // Spam
                "You're an idiot and I hate you!" // Abusive
            )

            testMessages.forEach { message ->
                val result = classifier.classifyMessage(message)
                Log.d(TAG, "Message: '$message'")
                Log.d(TAG, "Classification: ${result.threatType} (${result.confidence})")
                Log.d(TAG, "Is Threat: ${result.isThreat}")
                Log.d(TAG, "---")
            }

            Log.d(TAG, "✅ Basic classification test completed")
        }
    }

    private suspend fun testThreatDetectionPatterns() {
        withContext(Dispatchers.IO) {
            Log.d(TAG, "Testing threat detection patterns...")

            val detector = ThreatDetector()
            val testMessages = listOf(
                "Click here to verify your bank account immediately!",
                "Congratulations! You've won a lottery prize!",
                "Buy now! Limited time offer! Don't miss out!",
                "I'm going to hurt you badly, you deserve it!"
            )

            val analysis = detector.analyzePatterns(testMessages)

            Log.d(TAG, "Pattern Analysis Results:")
            Log.d(TAG, "Total Messages: ${analysis.totalMessages}")
            Log.d(TAG, "Phishing Attempts: ${analysis.phishingAttempts}")
            Log.d(TAG, "Spam Messages: ${analysis.spamMessages}")
            Log.d(TAG, "Abusive Content: ${analysis.abusiveContent}")
            Log.d(TAG, "Overall Risk Score: ${analysis.overallRiskScore}")

            Log.d(TAG, "✅ Pattern detection test completed")
        }
    }

    private suspend fun testModelManagerIntegration() {
        withContext(Dispatchers.IO) {
            Log.d(TAG, "Testing ModelManager integration...")

            val modelManager = ModelManager(context)

            // Wait a bit for model initialization
            kotlinx.coroutines.delay(2000)

            val modelInfo = modelManager.getModelInfo()
            Log.d(TAG, "Model Info:")
            Log.d(TAG, "Name: ${modelInfo.modelName}")
            Log.d(TAG, "Version: ${modelInfo.version}")
            Log.d(TAG, "Input Size: ${modelInfo.inputSize}")
            Log.d(TAG, "Output Classes: ${modelInfo.outputClasses}")
            Log.d(TAG, "Is Loaded: ${modelInfo.isLoaded}")

            // Test classification through ModelManager
            val testResult = modelManager.classifyMessage("Test message for ModelManager")
            if (testResult != null) {
                Log.d(TAG, "ModelManager classification successful: ${testResult.threatType}")
            } else {
                Log.w(TAG, "ModelManager classification returned null")
            }

            Log.d(TAG, "✅ ModelManager integration test completed")
        }
    }

    fun runSocialEngineeringTests() {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d(TAG, "Testing social engineering detection...")

            val detector = ThreatDetector()
            val testMessage = "URGENT: Your bank account will be suspended! Click here immediately to verify your identity!"

            val analysis = detector.detectSocialEngineering(testMessage)

            Log.d(TAG, "Social Engineering Analysis:")
            Log.d(TAG, "Urgency Score: ${analysis.urgencyScore}")
            Log.d(TAG, "Authority Score: ${analysis.authorityScore}")
            Log.d(TAG, "Emotional Score: ${analysis.emotionalScore}")
            Log.d(TAG, "Scarcity Score: ${analysis.scarcityScore}")
            Log.d(TAG, "Overall Score: ${analysis.overallScore}")
            Log.d(TAG, "Is Social Engineering: ${analysis.isSocialEngineering}")

            Log.d(TAG, "✅ Social engineering test completed")
        }
    }
}
