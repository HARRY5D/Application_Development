package com.example.sgp.encryption

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.io.File
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

    // Master key for encryption using Android Security library
    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

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

    fun encrypt(plainText: String): String? {
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)

            val secretKey = keyStore.getKey(MASTER_KEY_ALIAS, null) as SecretKey
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)

            val iv = cipher.iv
            val encryptedData = cipher.doFinal(plainText.toByteArray())

            // Combine IV and encrypted data
            val encryptedWithIv = iv + encryptedData
            Base64.encodeToString(encryptedWithIv, Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e(TAG, "Error encrypting data", e)
            null
        }
    }

    fun decrypt(encryptedText: String): String? {
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)

            val secretKey = keyStore.getKey(MASTER_KEY_ALIAS, null) as SecretKey
            val encryptedWithIv = Base64.decode(encryptedText, Base64.DEFAULT)

            // Extract IV and encrypted data
            val iv = encryptedWithIv.sliceArray(0..GCM_IV_LENGTH - 1)
            val encryptedData = encryptedWithIv.sliceArray(GCM_IV_LENGTH..encryptedWithIv.lastIndex)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            val decryptedData = cipher.doFinal(encryptedData)
            String(decryptedData)
        } catch (e: Exception) {
            Log.e(TAG, "Error decrypting data", e)
            null
        }
    }

    fun encryptMessage(message: String): String? {
        return encrypt(message)
    }

    fun decryptMessage(encryptedMessage: String): String? {
        return decrypt(encryptedMessage)
    }

    // Additional method using Android Security library for file encryption
    fun createEncryptedFile(fileName: String): EncryptedFile {
        val file = File(context.filesDir, fileName)
        return EncryptedFile.Builder(
            context,
            file,
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()
    }

    fun isKeyAvailable(): Boolean {
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)
            keyStore.containsAlias(MASTER_KEY_ALIAS)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking key availability", e)
            false
        }
    }

    fun deleteKey() {
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)
            keyStore.deleteEntry(MASTER_KEY_ALIAS)
            Log.d(TAG, "Master key deleted from Android Keystore")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting master key", e)
        }
    }
}
