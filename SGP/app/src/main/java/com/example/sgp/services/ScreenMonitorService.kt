package com.example.sgp.services

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.sgp.ml.MessageClassifier
import com.example.sgp.utils.NotificationUtils

class ScreenMonitorService : AccessibilityService() {

    private val targetPackages = listOf(
        "com.whatsapp",
        "org.telegram.messenger",
        "com.facebook.orca",
        "com.google.android.apps.messaging",
        "com.android.mms",
        "com.samsung.android.messaging"
    )

    private var lastProcessedText = ""
    private var notificationUtils: NotificationUtils? = null

    override fun onCreate() {
        super.onCreate()
        notificationUtils = NotificationUtils(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString() ?: return

        // Only process events from messaging apps
        if (!targetPackages.contains(packageName)) {
            return
        }

        // Check if this is a relevant event for text processing
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED ||
            event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||
            event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

            // Extract text from the current screen
            val rootNode = rootInActiveWindow ?: return
            val textMessages = extractTextFromNode(rootNode)

            // Process any new text found
            for (text in textMessages) {
                if (text != lastProcessedText && text.length > 10) {
                    lastProcessedText = text
                    processText(text, packageName)
                }
            }
        }
    }

    private fun extractTextFromNode(node: AccessibilityNodeInfo): List<String> {
        val result = mutableListOf<String>()

        // Extract text from this node if available
        if (node.text != null && !node.text.isBlank()) {
            result.add(node.text.toString())
        }

        // Recursively extract text from child nodes
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            result.addAll(extractTextFromNode(child))
            child.recycle()
        }

        return result
    }

    private fun processText(text: String, packageName: String) {
        // Use the MessageClassifier to check if this is a suspicious message
        val messageClassifier = MessageClassifier(applicationContext)
        val result = messageClassifier.classifyMessage(text)

        if (result.isThreat) {
            // Display an alert for the suspicious text
            notificationUtils?.showSecurityAlert(
                "Unknown",
                text,
                result.threatType,
                result.confidence
            )

            // Log the detection
            logDetection("On-screen text", text, packageName, result.threatType, result.confidence)
        }
    }

    private fun logDetection(
        source: String,
        message: String,
        packageName: String,
        threatType: String,
        confidenceScore: Float
    ) {
        // This would typically save to a local database
        // For simplicity, we're not implementing the full database functionality here
    }

    override fun onInterrupt() {
        // Service was interrupted
    }
}
