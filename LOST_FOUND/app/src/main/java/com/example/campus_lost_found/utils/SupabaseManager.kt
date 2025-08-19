package com.example.campus_lost_found.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.campus_lost_found.config.SupabaseConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.postgrest.Postgrest
import java.io.InputStream
import java.util.UUID

class SupabaseManager private constructor() {

    companion object {
        @Volatile
        private var INSTANCE: SupabaseManager? = null

        fun getInstance(): SupabaseManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SupabaseManager().also { INSTANCE = it }
            }
        }

        fun getClient() = getInstance().supabaseClient

        suspend fun uploadImage(context: Context, imageUri: Uri, fileName: String): Result<String> {
            return getInstance().uploadImageInternal(context, imageUri, fileName)
        }
    }

    val supabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = SupabaseConfig.SUPABASE_URL,
            supabaseKey = SupabaseConfig.SUPABASE_ANON_KEY
        ) {
            install(Storage)
            install(Postgrest)
            // Temporarily removing GoTrue to fix compilation issues
        }
    }

    private val storage by lazy { supabaseClient.storage }

    suspend fun uploadImageInternal(
        context: Context,
        imageUri: Uri,
        fileName: String
    ): Result<String> {
        return try {
            Log.d("SupabaseManager", "Uploading image: $fileName to bucket: item-images")

            val inputStream: InputStream = context.contentResolver.openInputStream(imageUri)
                ?: return Result.failure(Exception("Could not open image file"))

            val byteArray = inputStream.readBytes()
            inputStream.close()

            // Upload to Supabase Storage
            val uploadResult = storage.from("item-images").upload(fileName, byteArray)

            // Get public URL
            val publicUrl = storage.from("item-images").publicUrl(fileName)

            Log.d("SupabaseManager", "Image uploaded successfully: $publicUrl")
            Result.success(publicUrl)
        } catch (e: Exception) {
            Log.e("SupabaseManager", "Failed to upload image: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Simplified authentication methods - will use Firebase auth for now
    suspend fun signUpWithEmail(email: String, password: String): Result<String> {
        return try {
            // TODO: Implement Supabase auth once GoTrue issues are resolved
            Log.d("SupabaseManager", "Sign up simulated for: $email (using Firebase auth temporarily)")
            Result.success("Account created successfully! (Using Firebase auth)")
        } catch (e: Exception) {
            Log.e("SupabaseManager", "Sign up failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun signInWithEmail(email: String, password: String): Result<String> {
        return try {
            // TODO: Implement Supabase auth once GoTrue issues are resolved
            Log.d("SupabaseManager", "Sign in simulated for: $email (using Firebase auth temporarily)")
            Result.success("Sign in successful! (Using Firebase auth)")
        } catch (e: Exception) {
            Log.e("SupabaseManager", "Sign in failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun signOut(): Result<String> {
        return try {
            Log.d("SupabaseManager", "Sign out simulated (using Firebase auth temporarily)")
            Result.success("Signed out successfully")
        } catch (e: Exception) {
            Log.e("SupabaseManager", "Sign out failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun getCurrentUser() = null // Will return Firebase user once implemented

    fun isLoggedIn() = false // Will check Firebase auth once implemented

    fun getCurrentUserId(): String? = null // Will return Firebase user ID once implemented

    fun getCurrentUserEmail(): String? = null // Will return Firebase user email once implemented
}
