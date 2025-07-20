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

    @Query("SELECT * FROM messages WHERE id = :messageId")
    fun getMessageById(messageId: String): LiveData<Message>

    @Query("SELECT * FROM messages WHERE senderId = :userId OR receiverId = :userId ORDER BY timestamp DESC")
    fun getMessagesForUser(userId: String): LiveData<List<Message>>

    @Query("SELECT * FROM messages WHERE (senderId = :userId1 AND receiverId = :userId2) OR (senderId = :userId2 AND receiverId = :userId1) ORDER BY timestamp ASC")
    fun getMessagesBetweenUsers(userId1: String, userId2: String): LiveData<List<Message>>

    @Query("UPDATE messages SET isRead = 1 WHERE receiverId = :userId AND senderId = :otherId AND isRead = 0")
    suspend fun markMessagesAsRead(userId: String, otherId: String)

    @Query("DELETE FROM messages WHERE isSelfDestruct = 1 AND selfDestructTime <= :currentTime")
    suspend fun deleteSelfDestructMessages(currentTime: Long)
}
