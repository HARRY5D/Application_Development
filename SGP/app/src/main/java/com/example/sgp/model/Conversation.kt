package com.example.sgp.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.sgp.data.Converters

@Entity(tableName = "conversations")
@TypeConverters(Converters::class)
data class Conversation(
    @PrimaryKey val id: String,
    val participantIds: List<String>,
    val lastMessageId: String? = null,
    val lastMessagePreview: String? = null,
    val lastMessageTime: Long = 0,
    val unreadCount: Int = 0,
    val isGroup: Boolean = false,
    val groupName: String? = null,
    val groupPhotoUrl: String? = null
)
