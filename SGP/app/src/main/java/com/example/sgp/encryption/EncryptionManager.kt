package com.example.sgp.encryption

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class EncryptionManager(private val context: Context) {

    private val TAG = "EncryptionManager"
    private val ANDROID_KEYSTORE = "AndroidKeyStore"
    private val MASTER_KEY_ALIAS = "secure_chat_master_key"
    private val GCM_IV_LENGTH = 12
    private val TRANSFORMATION = "AES/GCM/NoPadding"

    init {
        // Initialize the master key for encryption
        initializeMasterKey()
    }

    private fun initializeMasterKey() {
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)

            // Check if the key already exists
            if (!keyStore.containsAlias(MASTER_KEY_ALIAS)) {
                // Generate a new key
                val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
                val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                    MASTER_KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(false)
                    .build()

                keyGenerator.init(keyGenParameterSpec)
                keyGenerator.generateKey()
                Log.d(TAG, "Master key generated and stored in Android Keystore")
            } else {
                Log.d(TAG, "Master key already exists in Android Keystore")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing master key", e)
        }
    }

    /**
     * Encrypt a message using AES-GCM encryption
     */
    fun encryptMessage(message: String): String {
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)

            val secretKey = keyStore.getKey(MASTER_KEY_ALIAS, null) as SecretKey
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)

            val iv = cipher.iv
            val encryptedData = cipher.doFinal(message.toByteArray())

            // Combine IV + encrypted data
            val result = ByteArray(iv.size + encryptedData.size)
            System.arraycopy(iv, 0, result, 0, iv.size)
            System.arraycopy(encryptedData, 0, result, iv.size, encryptedData.size)

            return Base64.encodeToString(result, Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e(TAG, "Error encrypting message", e)
            // For demo purposes, return the original message if encryption fails
            return message
        }
    }

    /**
     * Decrypt a message using AES-GCM decryption
     */
    fun decryptMessage(encryptedMessage: String): String {
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)

            val secretKey = keyStore.getKey(MASTER_KEY_ALIAS, null) as SecretKey
            val encryptedData = Base64.decode(encryptedMessage, Base64.DEFAULT)

            // Extract IV and encrypted data
            val iv = ByteArray(GCM_IV_LENGTH)
            val cipherText = ByteArray(encryptedData.size - GCM_IV_LENGTH)
            System.arraycopy(encryptedData, 0, iv, 0, iv.size)
            System.arraycopy(encryptedData, iv.size, cipherText, 0, cipherText.size)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            val decryptedData = cipher.doFinal(cipherText)
            return String(decryptedData)
        } catch (e: Exception) {
            Log.e(TAG, "Error decrypting message", e)
            // For demo purposes, return the encrypted message if decryption fails
            return encryptedMessage
        }
    }

    /**
     * Generate a mock public key for demonstration
     */
    fun getPublicKey(): String {
        return "mock_public_key_${System.currentTimeMillis()}"
    }

    /**
     * Generate a mock private key for demonstration
     */
    fun getPrivateKey(): String {
        return "mock_private_key_${System.currentTimeMillis()}"
    }

    /**
     * Mock key exchange for demo purposes
     */
    fun performKeyExchange(otherUserPublicKey: String): Boolean {
        Log.d(TAG, "Performing key exchange with: $otherUserPublicKey")
        // For demo purposes, always return true
        return true
    }

    /**
     * Check if encryption is available
     */
    fun isEncryptionAvailable(): Boolean {
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)
            keyStore.containsAlias(MASTER_KEY_ALIAS)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking encryption availability", e)
            false
        }
    }

    /**
     * Clear all encryption keys (for logout or reset)
     */
    fun clearKeys() {
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)
            keyStore.deleteEntry(MASTER_KEY_ALIAS)
            Log.d(TAG, "Encryption keys cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing keys", e)
        }
    }
}
