package com.example.sgp.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sgp.SecureChatApp
import com.example.sgp.adapter.MessageAdapter
import com.example.sgp.databinding.FragmentChatBinding
import com.example.sgp.model.Message
import com.example.sgp.viewmodel.MainViewModel
import com.example.sgp.viewmodel.MessageClassificationViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    
    private val args: ChatFragmentArgs by navArgs()
    private lateinit var adapter: MessageAdapter
    private val messages = mutableListOf<Message>()
    private var isSelfDestructEnabled = false

    // ViewModels
    private lateinit var mainViewModel: MainViewModel
    private lateinit var classificationViewModel: MessageClassificationViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViewModels()
        setupToolbar()
        setupRecyclerView()
        setupMessageInput()
        observeViewModels()

        // Load messages for the conversation
        loadConversationMessages()
    }

    private fun setupViewModels() {
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        classificationViewModel = ViewModelProvider(this)[MessageClassificationViewModel::class.java]
    }
    
    private fun setupToolbar() {
        binding.toolbarChat.title = args.userName
        binding.toolbarChat.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun setupRecyclerView() {
        adapter = MessageAdapter { message ->
            // Handle message click - show threat details if applicable
            if (message.isThreat) {
                showThreatDetails(message)
            }
        }

        binding.recyclerViewMessages.apply {
            this.adapter = this@ChatFragment.adapter
            layoutManager = LinearLayoutManager(requireContext()).apply {
                reverseLayout = true
                stackFromEnd = true
            }
        }
    }
    
    private fun setupMessageInput() {
        binding.buttonSend.setOnClickListener {
            val messageText = binding.editTextMessage.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
                binding.editTextMessage.text?.clear()
            }
        }
    }
    
    private fun observeViewModels() {
        // Observe message classification results
        classificationViewModel.classificationResult.observe(viewLifecycleOwner) { result ->
            // Handle classification result - could show immediate feedback
            if (result.isThreat) {
                showThreatWarning(result.threatType, result.confidence)
            }
        }

        // Observe loading state - handle without progressBar since it doesn't exist in layout
        mainViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Could show loading in other ways like disabling send button or showing toast
            // For now, just handle silently
        }
        
        // Observe errors
        mainViewModel.error.observe(viewLifecycleOwner) { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun sendMessage(content: String) {
        // First classify the message
        classificationViewModel.classifyMessage(content)

        // Process message through MainViewModel
        mainViewModel.processMessage(content, args.userId)

        // Create local message for immediate UI update
        val message = Message(
            id = UUID.randomUUID().toString(),
            conversationId = args.userId,
            senderId = "current_user",
            content = content,
            timestamp = System.currentTimeMillis(),
            isRead = true,
            isThreat = false, // Will be updated after classification
            threatType = "",
            confidenceScore = 0.0f
        )
        
        messages.add(0, message)
        adapter.submitList(messages.toList())
    }

    private fun loadConversationMessages() {
        // Load messages from database for this conversation
        mainViewModel.allMessages.observe(viewLifecycleOwner) { allMessages ->
            val conversationMessages = allMessages.filter { it.conversationId == args.userId }
            messages.clear()
            messages.addAll(conversationMessages.sortedByDescending { it.timestamp })
            adapter.submitList(messages.toList())
        }
    }

    private fun showThreatDetails(message: Message) {
        val details = """
            Threat Type: ${message.threatType.uppercase()}
            Confidence: ${(message.confidenceScore * 100).toInt()}%
            Message: ${message.content}
        """.trimIndent()

        Toast.makeText(requireContext(), details, Toast.LENGTH_LONG).show()
    }

    private fun showThreatWarning(threatType: String, confidence: Float) {
        val warning = "⚠️ Potential $threatType detected (${(confidence * 100).toInt()}% confidence)"
        Toast.makeText(requireContext(), warning, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
