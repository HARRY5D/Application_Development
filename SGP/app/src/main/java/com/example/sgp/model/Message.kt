package com.example.sgp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey val id: String,
    val conversationId: String, // For grouping messages by conversation
    val senderId: String,
    val receiverId: String? = null, // Optional for backward compatibility
    val content: String, // Decrypted content for display
    val encryptedContent: String? = null, // Encrypted version for storage
    val timestamp: Long,
    val isRead: Boolean = false,
    val isThreat: Boolean = false, // AI threat detection result
    val threatType: String = "", // Type of threat (phishing, spam, abusive)
    val confidenceScore: Float = 0f, // AI confidence score
    val isPhishingDetected: Boolean = false, // Legacy field for compatibility
    val phishingConfidence: Float = 0f, // Legacy field for compatibility
    val isSelfDestruct: Boolean = false,
    val selfDestructTime: Long? = null
)
