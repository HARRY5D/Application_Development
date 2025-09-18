package com.example.sgp.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.sgp.data.Converters

@Entity(tableName = "conversations")
@TypeConverters(Converters::class)
data class Conversation(
    @PrimaryKey val id: String,
    val name: String, // Display name for the conversation
    val participantIds: List<String>,
    val lastMessage: String? = null, // Last message preview
    val lastMessageId: String? = null,
    val lastMessageTime: Long = 0,
    val unreadCount: Int = 0,
    val hasThreat: Boolean = false, // Whether conversation has threat messages
    val isOnline: Boolean = false, // Online status of participant
    val isGroup: Boolean = false,
    val groupName: String? = null,
    val groupPhotoUrl: String? = null
)
