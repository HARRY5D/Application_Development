package com.example.sgp

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.sgp.databinding.ActivityMainBinding
import com.example.sgp.services.ScreenMonitorService
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Prevent screenshots for security
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        // Set up Navigation Component
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Set up bottom navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setupWithNavController(navController)

        // Check for notifications from security alerts
        handleSecurityAlertNotifications(intent)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Handle notifications when app is already running
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleSecurityAlertNotifications(intent)
    }

    private fun handleSecurityAlertNotifications(intent: Intent) {
        // Check if we were launched from a security alert notification
        if (intent.hasExtra("DETECTED_MESSAGE")) {
            val message = intent.getStringExtra("DETECTED_MESSAGE") ?: return
            val sender = intent.getStringExtra("SENDER") ?: "Unknown"
            val threatType = intent.getStringExtra("THREAT_TYPE") ?: "UNKNOWN"
            val appName = intent.getStringExtra("APP_NAME") ?: "Unknown App"
            val confidence = intent.getFloatExtra("CONFIDENCE", 0.0f)
            val explanation = intent.getStringExtra("EXPLANATION") ?: "No explanation available"

            // Log the threat detection for debugging
            android.util.Log.w("MainActivity", "Security threat detected from notification - Type: $threatType, App: $appName, Confidence: ${(confidence * 100).toInt()}%")

            // Navigate to the dashboard and show threat details
            navController.navigate(R.id.dashboardFragment)

            // Create a bundle with threat details to pass to the dashboard
            val threatBundle = Bundle().apply {
                putString("threat_message", message)
                putString("threat_sender", sender)
                putString("threat_type", threatType)
                putString("threat_app", appName)
                putFloat("threat_confidence", confidence)
                putString("threat_explanation", explanation)
                putLong("threat_timestamp", System.currentTimeMillis())
            }

            // Show threat alert dialog
            showThreatAlertDialog(threatBundle)
        }
    }

    private fun showThreatAlertDialog(threatBundle: Bundle) {
        val message = threatBundle.getString("threat_message", "")
        val sender = threatBundle.getString("threat_sender", "Unknown")
        val threatType = threatBundle.getString("threat_type", "UNKNOWN")
        val appName = threatBundle.getString("threat_app", "Unknown App")
        val confidence = threatBundle.getFloat("threat_confidence", 0.0f)
        val explanation = threatBundle.getString("threat_explanation", "")

        val threatTypeDisplay = when (threatType) {
            "phishing" -> "🎣 Phishing Attempt"
            "spam" -> "📧 Spam Message"
            "abusive" -> "⚠️ Abusive Content"
            else -> "🚨 Security Threat"
        }

        val confidencePercent = (confidence * 100).toInt()

        val alertDialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🛡️ Security Alert Detected")
            .setMessage("""
                $threatTypeDisplay detected with $confidencePercent% confidence
                
                📱 App: $appName
                👤 Sender: $sender
                
                📝 Message Preview:
                "${message.take(150)}${if (message.length > 150) "..." else ""}"
                
                💡 Analysis: $explanation
                
                ⚠️ This message may be attempting to deceive or harm you. Please be cautious before taking any action.
            """.trimIndent())
            .setPositiveButton("View Details") { _, _ ->
                // Navigate to a detailed threat view (could be implemented later)
                android.util.Log.d("MainActivity", "User wants to view threat details")
            }
            .setNegativeButton("Dismiss") { dialog, _ ->
                dialog.dismiss()
            }
            .setNeutralButton("Block Sender") { _, _ ->
                // TODO: Implement sender blocking functionality
                android.util.Log.d("MainActivity", "User wants to block sender: $sender")
                android.widget.Toast.makeText(this, "Blocking functionality will be implemented in future updates", android.widget.Toast.LENGTH_SHORT).show()
            }
            .setCancelable(true)
            .create()

        alertDialog.show()
    }

    // Disable back button in certain screens
    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        val currentDestination = navController.currentDestination?.id
        when (currentDestination) {
            R.id.permissionsFragment -> {
                // Exit app instead of going back from permissions screen
                finish()
            }
            else -> {
                super.onBackPressed()
            }
        }
    }

    // Helper methods for permission checking
    fun isNotificationListenerEnabled(): Boolean {
        return com.example.sgp.utils.NotificationAccessHelper.isOurNotificationServiceEnabled(this)
    }

    fun isAccessibilityServiceEnabled(): Boolean {
        val serviceName = "$packageName/${ScreenMonitorService::class.java.canonicalName}"
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabledServices.contains(serviceName)
    }

    // Navigation helper methods
    fun openNotificationListenerSettings() {
        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
    }

    fun openAccessibilitySettings() {
        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }

    fun navigateToDashboard() {
        navController.navigate(R.id.dashboardFragment)
    }

    /**
     * Manually trigger notification history scan
     * This will process existing notifications from messaging apps
     */
    fun triggerNotificationHistoryScan() {
        android.util.Log.d("MainActivity", "Triggering manual notification history scan")

        if (!isNotificationListenerEnabled()) {
            android.widget.Toast.makeText(
                this,
                "Notification access not enabled. Please enable it in settings.",
                android.widget.Toast.LENGTH_LONG
            ).show()
            return
        }

        // Try to trigger the scan by sending a broadcast to the service
        val intent = Intent("com.example.sgp.REFRESH_NOTIFICATION_SCAN")
        intent.setPackage(packageName)
        sendBroadcast(intent)

        android.widget.Toast.makeText(
            this,
            "🔍 Scanning notification history for threats...",
            android.widget.Toast.LENGTH_SHORT
        ).show()

        // Log the scan request
        com.example.sgp.utils.NotificationAccessHelper.logNotificationServiceStatus(this)
    }

    /**
     * Check and request notification access if needed
     */
    fun ensureNotificationAccess(): Boolean {
        return if (isNotificationListenerEnabled()) {
            true
        } else {
            // Show dialog to guide user to settings
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("🔔 Notification Access Required")
                .setMessage("""
                    To scan messages for security threats, this app needs access to notifications.
                    
                    📱 This will allow the app to:
                    • Monitor messages from WhatsApp, Telegram, SMS, etc.
                    • Detect phishing and spam attempts
                    • Alert you about suspicious content
                    
                    🔒 Your privacy is protected - messages are analyzed locally and never sent to external servers.
                """.trimIndent())
                .setPositiveButton("Open Settings") { _, _ ->
                    openNotificationListenerSettings()
                }
                .setNegativeButton("Cancel", null)
                .show()
            false
        }
    }
}