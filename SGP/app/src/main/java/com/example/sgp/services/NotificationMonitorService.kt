package com.example.sgp.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import com.example.sgp.MainActivity
import com.example.sgp.R
import com.example.sgp.ml.SEDetectionModel
import java.util.regex.Pattern

class NotificationMonitorService : NotificationListenerService() {

    private val targetPackages = listOf(
        "com.whatsapp",
        "org.telegram.messenger",
        "com.facebook.orca",
        "com.google.android.apps.messaging",
        "com.android.mms",
        "com.samsung.android.messaging"
    )

    // Channel ID for our security alert notifications
    private val CHANNEL_ID = "security_alerts"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // Check if the notification is from a messaging app we're interested in
        if (!targetPackages.contains(sbn.packageName)) {
            return
        }

        val extras = sbn.notification.extras
        val title = extras.getString("android.title") ?: return
        val text = extras.getCharSequence("android.text")?.toString() ?: return

        // Process the notification content
        processMessage(title, text, sbn.packageName)
    }

    private fun processMessage(sender: String, message: String, packageName: String) {
        // Use TFLite model to check if this is a suspicious message
        val result = SEDetectionModel.getInstance(applicationContext).detectThreat(message)

        if (result.isSuspicious()) {
            // Send an alert notification
            sendSecurityAlert(sender, message, result.threatType, result.confidenceScore)

            // Log the detection for the app's history
            logDetection(sender, message, packageName, result.threatType, result.confidenceScore)
        }
    }

    private fun sendSecurityAlert(sender: String, message: String, threatType: String, confidenceScore: Float) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("DETECTED_MESSAGE", message)
            putExtra("SENDER", sender)
            putExtra("THREAT_TYPE", threatType)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val threatTypeString = when (threatType) {
            "PHISHING" -> getString(R.string.phishing)
            "SMISHING" -> getString(R.string.smishing)
            else -> threatType.lowercase()
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_security_warning)
            .setContentTitle("Security Alert")
            .setContentText(getString(R.string.warning_message, threatTypeString))
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Message from $sender may contain $threatTypeString attempt:\n\n\"$message\""))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Security Alerts"
            val descriptionText = "Notifications about detected security threats"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun logDetection(
        sender: String,
        message: String,
        packageName: String,
        threatType: String,
        confidenceScore: Float
    ) {
        // This would typically save to a local database
        // For simplicity, we're not implementing the full database functionality here
    }
}
