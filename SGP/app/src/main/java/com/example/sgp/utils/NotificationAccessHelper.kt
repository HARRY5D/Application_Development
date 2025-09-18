package com.example.sgp.utils

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.text.TextUtils
import android.util.Log

object NotificationAccessHelper {

    private const val TAG = "NotificationAccessHelper"

    /**
     * Check if notification listener service is enabled
     */
    fun isNotificationServiceEnabled(context: Context): Boolean {
        val packageName = context.packageName
        val flat = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )

        if (!TextUtils.isEmpty(flat)) {
            val names = flat.split(":")
            for (name in names) {
                val componentName = ComponentName.unflattenFromString(name)
                if (componentName != null) {
                    if (TextUtils.equals(packageName, componentName.packageName)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    /**
     * Get the expected component name for our notification listener service
     */
    fun getNotificationServiceComponent(context: Context): ComponentName {
        return ComponentName(
            context,
            "com.example.sgp.services.NotificationMonitorService"
        )
    }

    /**
     * Check if our specific notification listener service is enabled
     */
    fun isOurNotificationServiceEnabled(context: Context): Boolean {
        val expectedComponentName = getNotificationServiceComponent(context).flattenToString()
        val enabledNotificationListeners = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )

        return enabledNotificationListeners?.contains(expectedComponentName) == true
    }

    /**
     * Log current notification listener status for debugging
     */
    fun logNotificationServiceStatus(context: Context) {
        val isEnabled = isOurNotificationServiceEnabled(context)
        val componentName = getNotificationServiceComponent(context)

        Log.d(TAG, """
            Notification Service Status:
            Component: $componentName
            Is Enabled: $isEnabled
            Package: ${context.packageName}
        """.trimIndent())

        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )
        Log.d(TAG, "All enabled notification listeners: $enabledServices")
    }
}
