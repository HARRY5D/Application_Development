package com.example.sgp.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sgp.R
import com.example.sgp.adapter.ConversationAdapter
import com.example.sgp.databinding.FragmentConversationsBinding
import com.example.sgp.model.Conversation
import com.example.sgp.viewmodel.MainViewModel
import com.example.sgp.viewmodel.MessageClassificationViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ConversationsFragment : Fragment() {

    private var _binding: FragmentConversationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ConversationAdapter
    private val conversations = mutableListOf<Conversation>()

    // ViewModels
    private lateinit var mainViewModel: MainViewModel
    private lateinit var classificationViewModel: MessageClassificationViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConversationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModels()
        setupToolbar()
        setupRecyclerView()
        observeViewModels()
        loadConversations()

        binding.fabNewChat.setOnClickListener {
            // In a real app, this would show a user selection dialog
            Toast.makeText(requireContext(), "New chat feature coming soon!", Toast.LENGTH_SHORT).show()

            // For demo purposes, navigate to a mock chat
            val action = ConversationsFragmentDirections.actionConversationsToChat(
                userId = "demo_user_${System.currentTimeMillis()}",
                userName = "Demo User"
            )
            findNavController().navigate(action)
        }
    }

    private fun setupViewModels() {
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        classificationViewModel = ViewModelProvider(this)[MessageClassificationViewModel::class.java]
    }

    private fun setupToolbar() {
        binding.toolbar.title = "Secure Chat Lite"
    }

    private fun setupRecyclerView() {
        adapter = ConversationAdapter { conversation ->
            // Navigate to chat with the selected conversation
            val action = ConversationsFragmentDirections.actionConversationsToChat(
                userId = conversation.id,
                userName = conversation.name
            )
            findNavController().navigate(action)
        }

        binding.recyclerViewConversations.apply {
            this.adapter = this@ConversationsFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeViewModels() {
        // Observe all messages to update conversation previews
        mainViewModel.allMessages.observe(viewLifecycleOwner) { messages ->
            updateConversationsFromMessages(messages)
        }

        // Observe threat statistics
        classificationViewModel.threatStats.observe(viewLifecycleOwner) { stats ->
            updateThreatBadge(stats.threatsDetected)
        }

        // Observe loading state - hide loading since progressBar doesn't exist in layout
        mainViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // No progressBar in layout, could add one or handle loading differently
        }
    }

    private fun loadConversations() {
        // Load mock conversations for demo
        loadMockConversations()
    }

    private fun loadMockConversations() {
        conversations.clear()

        // Add some demo conversations with different threat levels
        conversations.addAll(listOf(
            Conversation(
                id = "conv_1",
                name = "Alice Johnson",
                participantIds = listOf("current_user", "alice_123"),
                lastMessage = "Hey, how are you doing?",
                lastMessageTime = System.currentTimeMillis() - 3600000,
                unreadCount = 2,
                hasThreat = false,
                isOnline = true
            ),
            Conversation(
                id = "conv_2",
                name = "Suspicious Sender",
                participantIds = listOf("current_user", "suspicious_456"),
                lastMessage = "URGENT: Verify your account immediately!",
                lastMessageTime = System.currentTimeMillis() - 7200000,
                unreadCount = 1,
                hasThreat = true,
                isOnline = false
            ),
            Conversation(
                id = "conv_3",
                name = "Work Group",
                participantIds = listOf("current_user", "work_789", "colleague_101"),
                lastMessage = "Meeting scheduled for tomorrow",
                lastMessageTime = System.currentTimeMillis() - 86400000,
                unreadCount = 0,
                hasThreat = false,
                isOnline = true
            ),
            Conversation(
                id = "conv_4",
                name = "Spam Account",
                participantIds = listOf("current_user", "spam_999"),
                lastMessage = "Win $1000 now! Click here for amazing deals!",
                lastMessageTime = System.currentTimeMillis() - 172800000,
                unreadCount = 5,
                hasThreat = true,
                isOnline = false
            )
        ))

        adapter.submitList(conversations.toList())
    }

    private fun updateConversationsFromMessages(messages: List<com.example.sgp.model.Message>) {
        // Group messages by conversation and update conversation previews
        val conversationMap = messages.groupBy { it.conversationId }

        conversations.forEach { conversation ->
            val conversationMessages = conversationMap[conversation.id]
            if (!conversationMessages.isNullOrEmpty()) {
                val latestMessage = conversationMessages.maxByOrNull { it.timestamp }
                latestMessage?.let { msg ->
                    val updatedConversation = conversation.copy(
                        lastMessage = msg.content,
                        lastMessageTime = msg.timestamp,
                        hasThreat = msg.isThreat,
                        unreadCount = conversationMessages.count { !it.isRead }
                    )

                    val index = conversations.indexOf(conversation)
                    if (index != -1) {
                        conversations[index] = updatedConversation
                    }
                }
            }
        }

        // Sort by most recent message
        conversations.sortByDescending { it.lastMessageTime }
        adapter.submitList(conversations.toList())
    }

    private fun updateThreatBadge(threatCount: Int) {
        // Update UI to show total threat count in toolbar
        if (threatCount > 0) {
            binding.toolbar.subtitle = "$threatCount threats detected"
        } else {
            binding.toolbar.subtitle = "All clear"
        }
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        _binding = null
    }
}
