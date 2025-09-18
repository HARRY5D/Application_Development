package com.example.sgp.ml

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ModelManager(private val context: Context) {

    private var messageClassifier: MessageClassifier? = null
    private val threatDetector = ThreatDetector()

    private val _modelStatus = MutableLiveData<ModelStatus>()
    val modelStatus: LiveData<ModelStatus> = _modelStatus

    private val _isInitialized = MutableLiveData<Boolean>()
    val isInitialized: LiveData<Boolean> = _isInitialized

    init {
        initializeModels()
    }

    private fun initializeModels() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                _modelStatus.postValue(ModelStatus.LOADING)

                // Initialize TensorFlow Lite model
                messageClassifier = MessageClassifier(context)

                // Verify model is working
                val testResult = messageClassifier?.classifyMessage("Test message")

                if (testResult != null) {
                    _modelStatus.postValue(ModelStatus.READY)
                    _isInitialized.postValue(true)
                } else {
                    _modelStatus.postValue(ModelStatus.ERROR)
                    _isInitialized.postValue(false)
                }

            } catch (e: Exception) {
                _modelStatus.postValue(ModelStatus.ERROR)
                _isInitialized.postValue(false)
            }
        }
    }

    suspend fun classifyMessage(message: String): MessageClassifier.ClassificationResult? {
        return withContext(Dispatchers.IO) {
            try {
                messageClassifier?.classifyMessage(message)
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun batchClassifyMessages(messages: List<String>): List<MessageClassifier.ClassificationResult> {
        return withContext(Dispatchers.IO) {
            messages.mapNotNull { message ->
                try {
                    messageClassifier?.classifyMessage(message)
                } catch (e: Exception) {
                    null
                }
            }
        }
    }

    suspend fun analyzeMessagePatterns(messages: List<String>): ThreatDetector.PatternAnalysis {
        return withContext(Dispatchers.IO) {
            threatDetector.analyzePatterns(messages)
        }
    }

    suspend fun detectSocialEngineering(message: String): ThreatDetector.SocialEngineeringAnalysis {
        return withContext(Dispatchers.IO) {
            threatDetector.detectSocialEngineering(message)
        }
    }

    fun getModelInfo(): ModelInfo {
        return ModelInfo(
            modelName = "TinyBERT Quantized",
            version = "1.0",
            inputSize = 128,
            outputClasses = listOf("safe", "spam", "phishing", "abusive"),
            isLoaded = _isInitialized.value ?: false
        )
    }

    fun reloadModel() {
        messageClassifier?.close()
        initializeModels()
    }

    fun shutdown() {
        messageClassifier?.close()
        _isInitialized.postValue(false)
        _modelStatus.postValue(ModelStatus.UNLOADED)
    }

    enum class ModelStatus {
        LOADING,
        READY,
        ERROR,
        UNLOADED
    }

    data class ModelInfo(
        val modelName: String,
        val version: String,
        val inputSize: Int,
        val outputClasses: List<String>,
        val isLoaded: Boolean
    )
}
