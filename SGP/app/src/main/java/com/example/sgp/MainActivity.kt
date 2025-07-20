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
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.sgp.databinding.ActivityMainBinding
import com.example.sgp.services.ScreenMonitorService
import com.example.sgp.ui.DashboardFragment
import com.example.sgp.ui.PermissionsFragment
import com.example.sgp.ui.SettingsFragment
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

        // Set up app configuration
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.loginFragment, R.id.dashboardFragment, R.id.permissionsFragment, R.id.conversationsFragment)
        )

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

            // Navigate to the dashboard and show details
            navController.navigate(R.id.dashboardFragment)

            // TODO: Show threat details in the dashboard
        }
    }

    // Disable back button in certain screens
    override fun onBackPressed() {
        val currentDestination = navController.currentDestination?.id
        when (currentDestination) {
            R.id.permissionsFragment -> {
                // Exit app instead of going back from login
                finish()
            }
            else -> {
                super.onBackPressed()
            }
        }
    }

    // Helper methods for permission checking
    fun isNotificationListenerEnabled(): Boolean {
        val packageName = packageName
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return flat?.contains(packageName) == true
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
}