package com.example.sgp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.sgp.data.SecureChatDatabase
import com.example.sgp.ml.MessageClassifier
import com.example.sgp.model.Message
import com.example.sgp.repository.MessageRepository
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MessageRepository
    private val messageClassifier: MessageClassifier

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _navigationEvent = MutableLiveData<NavigationEvent>()
    val navigationEvent: LiveData<NavigationEvent> = _navigationEvent

    val allMessages: LiveData<List<Message>>

    init {
        val database = SecureChatDatabase.getInstance(application)
        repository = MessageRepository(database)
        messageClassifier = MessageClassifier(application)
        allMessages = repository.getAllMessages()
    }

    fun processMessage(content: String, conversationId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Classify message using AI model
                val classificationResult = messageClassifier.classifyMessage(content)

                // Create message object
                val message = Message(
                    id = generateMessageId(),
                    conversationId = conversationId,
                    senderId = getCurrentUserId(),
                    content = content,
                    timestamp = System.currentTimeMillis(),
                    isRead = false,
                    isThreat = classificationResult.isThreat,
                    threatType = classificationResult.threatType,
                    confidenceScore = classificationResult.confidence
                )

                // Save to database
                repository.insertMessage(message)

                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = "Error processing message: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun searchMessages(query: String) {
        viewModelScope.launch {
            try {
                val results = repository.searchMessages(query)
                // Handle search results
            } catch (e: Exception) {
                _error.value = "Error searching messages: ${e.message}"
            }
        }
    }

    fun navigateToConversations() {
        _navigationEvent.value = NavigationEvent.ToConversations
    }

    fun navigateToSettings() {
        _navigationEvent.value = NavigationEvent.ToSettings
    }

    private fun generateMessageId(): String {
        return "msg_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }

    private fun getCurrentUserId(): String {
        // Return current user ID (implement based on your auth system)
        return "current_user_id"
    }

    sealed class NavigationEvent {
        object ToConversations : NavigationEvent()
        object ToSettings : NavigationEvent()
        data class ToChat(val conversationId: String) : NavigationEvent()
    }
}
