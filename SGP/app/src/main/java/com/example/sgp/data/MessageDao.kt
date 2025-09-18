package com.example.sgp.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.sgp.model.Message

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message)

    @Update
    suspend fun updateMessage(message: Message)

    @Delete
    suspend fun deleteMessage(message: Message)

    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: String)

    @Query("SELECT * FROM messages WHERE id = :messageId")
    suspend fun getMessageById(messageId: String): Message?

    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun getAllMessages(): LiveData<List<Message>>

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: String): LiveData<List<Message>>

    @Query("SELECT * FROM messages WHERE senderId = :userId OR receiverId = :userId ORDER BY timestamp DESC")
    fun getMessagesForUser(userId: String): LiveData<List<Message>>

    @Query("SELECT * FROM messages WHERE (senderId = :userId1 AND receiverId = :userId2) OR (senderId = :userId2 AND receiverId = :userId1) ORDER BY timestamp ASC")
    fun getMessagesBetweenUsers(userId1: String, userId2: String): LiveData<List<Message>>

    @Query("UPDATE messages SET isRead = 1 WHERE id = :messageId")
    suspend fun markAsRead(messageId: String)

    @Query("UPDATE messages SET isRead = 1 WHERE receiverId = :userId AND senderId = :otherId AND isRead = 0")
    suspend fun markMessagesAsRead(userId: String, otherId: String)

    @Query("SELECT COUNT(*) FROM messages WHERE isRead = 0")
    suspend fun getUnreadMessagesCount(): Int

    @Query("SELECT * FROM messages WHERE content LIKE :query ORDER BY timestamp DESC")
    suspend fun searchMessages(query: String): List<Message>

    @Query("DELETE FROM messages WHERE isSelfDestruct = 1 AND selfDestructTime <= :currentTime")
    suspend fun deleteSelfDestructMessages(currentTime: Long)
}
