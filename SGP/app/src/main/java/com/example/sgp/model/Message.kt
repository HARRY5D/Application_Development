package com.example.sgp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey val id: String,
    val senderId: String,
    val receiverId: String,
    val encryptedContent: String,
    val timestamp: Long,
    val isRead: Boolean = false,
    val isPhishingDetected: Boolean = false,
    val phishingConfidence: Float = 0f,
    val isSelfDestruct: Boolean = false,
    val selfDestructTime: Long? = null
)
