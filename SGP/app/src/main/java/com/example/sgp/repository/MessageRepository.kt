package com.example.sgp.repository

import androidx.lifecycle.LiveData
import com.example.sgp.data.SecureChatDatabase
import com.example.sgp.model.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MessageRepository(private val database: SecureChatDatabase) {

    fun getAllMessages(): LiveData<List<Message>> {
        return database.messageDao().getAllMessages()
    }

    fun getMessagesForConversation(conversationId: String): LiveData<List<Message>> {
        return database.messageDao().getMessagesForConversation(conversationId)
    }

    suspend fun insertMessage(message: Message) {
        withContext(Dispatchers.IO) {
            database.messageDao().insertMessage(message)
        }
    }

    suspend fun updateMessage(message: Message) {
        withContext(Dispatchers.IO) {
            database.messageDao().updateMessage(message)
        }
    }

    suspend fun deleteMessage(messageId: String) {
        withContext(Dispatchers.IO) {
            database.messageDao().deleteMessage(messageId)
        }
    }

    suspend fun getMessageById(messageId: String): Message? {
        return withContext(Dispatchers.IO) {
            database.messageDao().getMessageById(messageId)
        }
    }

    suspend fun markMessageAsRead(messageId: String) {
        withContext(Dispatchers.IO) {
            database.messageDao().markAsRead(messageId)
        }
    }

    suspend fun getUnreadMessagesCount(): Int {
        return withContext(Dispatchers.IO) {
            database.messageDao().getUnreadMessagesCount()
        }
    }

    suspend fun searchMessages(query: String): List<Message> {
        return withContext(Dispatchers.IO) {
            database.messageDao().searchMessages(query)
        }
    }
}
