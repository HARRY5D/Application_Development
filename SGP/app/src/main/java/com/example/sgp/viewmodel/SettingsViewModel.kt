package com.example.sgp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val _threatDetectionEnabled = MutableLiveData<Boolean>()
    val threatDetectionEnabled: LiveData<Boolean> = _threatDetectionEnabled

    private val _autoDeleteEnabled = MutableLiveData<Boolean>()
    val autoDeleteEnabled: LiveData<Boolean> = _autoDeleteEnabled

    private val _encryptionEnabled = MutableLiveData<Boolean>()
    val encryptionEnabled: LiveData<Boolean> = _encryptionEnabled

    private val _biometricEnabled = MutableLiveData<Boolean>()
    val biometricEnabled: LiveData<Boolean> = _biometricEnabled

    private val _notificationsEnabled = MutableLiveData<Boolean>()
    val notificationsEnabled: LiveData<Boolean> = _notificationsEnabled

    private val _confidenceThreshold = MutableLiveData<Float>()
    val confidenceThreshold: LiveData<Float> = _confidenceThreshold

    private val _settingsSaved = MutableLiveData<Boolean>()
    val settingsSaved: LiveData<Boolean> = _settingsSaved

    init {
        loadSettings()
    }

    fun toggleThreatDetection(enabled: Boolean) {
        _threatDetectionEnabled.value = enabled
        saveSettings()
    }

    fun toggleAutoDelete(enabled: Boolean) {
        _autoDeleteEnabled.value = enabled
        saveSettings()
    }

    fun toggleEncryption(enabled: Boolean) {
        _encryptionEnabled.value = enabled
        saveSettings()
    }

    fun toggleBiometric(enabled: Boolean) {
        _biometricEnabled.value = enabled
        saveSettings()
    }

    fun toggleNotifications(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        saveSettings()
    }

    fun updateConfidenceThreshold(threshold: Float) {
        _confidenceThreshold.value = threshold.coerceIn(0.0f, 1.0f)
        saveSettings()
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            _threatDetectionEnabled.value = true
            _autoDeleteEnabled.value = false
            _encryptionEnabled.value = true
            _biometricEnabled.value = false
            _notificationsEnabled.value = true
            _confidenceThreshold.value = 0.7f
            saveSettings()
        }
    }

    fun exportSettings(): String {
        val settings = mapOf(
            "threatDetection" to _threatDetectionEnabled.value,
            "autoDelete" to _autoDeleteEnabled.value,
            "encryption" to _encryptionEnabled.value,
            "biometric" to _biometricEnabled.value,
            "notifications" to _notificationsEnabled.value,
            "confidenceThreshold" to _confidenceThreshold.value
        )

        return settings.toString()
    }

    private fun loadSettings() {
        // Load from SharedPreferences or database
        _threatDetectionEnabled.value = getPreference("threat_detection", true)
        _autoDeleteEnabled.value = getPreference("auto_delete", false)
        _encryptionEnabled.value = getPreference("encryption", true)
        _biometricEnabled.value = getPreference("biometric", false)
        _notificationsEnabled.value = getPreference("notifications", true)
        _confidenceThreshold.value = getPreference("confidence_threshold", 0.7f)
    }

    private fun saveSettings() {
        viewModelScope.launch {
            try {
                // Save to SharedPreferences or database
                savePreference("threat_detection", _threatDetectionEnabled.value ?: true)
                savePreference("auto_delete", _autoDeleteEnabled.value ?: false)
                savePreference("encryption", _encryptionEnabled.value ?: true)
                savePreference("biometric", _biometricEnabled.value ?: false)
                savePreference("notifications", _notificationsEnabled.value ?: true)
                savePreference("confidence_threshold", _confidenceThreshold.value ?: 0.7f)

                _settingsSaved.value = true
            } catch (e: Exception) {
                _settingsSaved.value = false
            }
        }
    }

    private fun getPreference(key: String, defaultValue: Boolean): Boolean {
        // Implement SharedPreferences retrieval
        return defaultValue
    }

    private fun getPreference(key: String, defaultValue: Float): Float {
        // Implement SharedPreferences retrieval
        return defaultValue
    }

    private fun savePreference(key: String, value: Boolean) {
        // Implement SharedPreferences saving
    }

    private fun savePreference(key: String, value: Float) {
        // Implement SharedPreferences saving
    }
}
