package com.example.sgp.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.sgp.MainActivity
import com.example.sgp.R

class NotificationUtils(private val context: Context) {

    private val CHANNEL_ID = "security_alerts"

    init {
        createNotificationChannel()
    }

    fun showSecurityAlert(sender: String, message: String, threatType: String, confidenceScore: Float) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("DETECTED_MESSAGE", message)
            putExtra("SENDER", sender)
            putExtra("THREAT_TYPE", threatType)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val threatTypeString = when (threatType) {
            "PHISHING" -> context.getString(R.string.phishing)
            "SMISHING" -> context.getString(R.string.smishing)
            else -> threatType.lowercase()
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_security_warning)
            .setContentTitle("Security Alert")
            .setContentText(context.getString(R.string.warning_message, threatTypeString))
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

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
