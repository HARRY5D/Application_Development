package com.example.sgp.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.sgp.model.Conversation

@Dao
interface ConversationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: Conversation)

    @Update
    suspend fun updateConversation(conversation: Conversation)

    @Delete
    suspend fun deleteConversation(conversation: Conversation)

    @Query("SELECT * FROM conversations WHERE id = :conversationId")
    fun getConversationById(conversationId: String): LiveData<Conversation>

    @Query("SELECT * FROM conversations ORDER BY lastMessageTime DESC")
    fun getAllConversations(): LiveData<List<Conversation>>

    @Query("SELECT * FROM conversations WHERE :userId IN (SELECT participantIds FROM conversations) ORDER BY lastMessageTime DESC")
    fun getConversationsByUser(userId: String): LiveData<List<Conversation>>

    @Query("UPDATE conversations SET unreadCount = 0 WHERE id = :conversationId")
    suspend fun clearUnreadCount(conversationId: String)

    @Query("UPDATE conversations SET lastMessageId = :messageId, lastMessagePreview = :preview, lastMessageTime = :timestamp, unreadCount = unreadCount + :incrementUnread WHERE id = :conversationId")
    suspend fun updateLastMessage(conversationId: String, messageId: String, preview: String, timestamp: Long, incrementUnread: Int)
}
