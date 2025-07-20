package com.example.sgp.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sgp.databinding.FragmentChatBinding
import com.example.sgp.ml.SocialEngineeringDetector
import com.example.sgp.model.Message
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    
    private val args: ChatFragmentArgs by navArgs()
    private lateinit var adapter: MessagesAdapter
    private val messages = mutableListOf<Message>()
    private var isSelfDestructEnabled = false

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
        
        setupToolbar()
        setupRecyclerView()
        setupMessageInput()
        
        // Load mock messages for demo
        loadMockMessages()
    }
    
    private fun setupToolbar() {
        binding.toolbarChat.title = args.userName
        binding.textViewChatRecipientName.text = args.userName

        binding.toolbarChat.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun setupRecyclerView() {
        adapter = MessagesAdapter(requireContext(), "current_user_id")
        
        binding.recyclerViewMessages.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true // Show newest messages at bottom
            }
            adapter = this@ChatFragment.adapter
        }
    }
    
    private fun setupMessageInput() {
        // Self-destruct toggle
        binding.switchSelfDestruct.setOnCheckedChangeListener { _, isChecked ->
            isSelfDestructEnabled = isChecked
        }
        
        // Send button
        binding.buttonSend.setOnClickListener {
            val messageText = binding.editTextMessage.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
                binding.editTextMessage.text.clear()
            }
        }
    }
    
    private fun loadMockMessages() {
        binding.progressBarChat.visibility = View.VISIBLE
        
        // Add some mock messages for demonstration
        val currentTime = System.currentTimeMillis()
        val minuteMillis = 60 * 1000L
        
        messages.apply {
            clear()
            
            // Add older messages
            add(Message(
                id = UUID.randomUUID().toString(),
                senderId = args.userId,
                receiverId = "current_user_id",
                encryptedContent = "Hi there! How are you?",
                timestamp = currentTime - 30 * minuteMillis,
                isRead = true
            ))
            
            add(Message(
                id = UUID.randomUUID().toString(),
                senderId = "current_user_id",
                receiverId = args.userId,
                encryptedContent = "I'm doing great, thanks for asking!",
                timestamp = currentTime - 25 * minuteMillis,
                isRead = true
            ))
            
            add(Message(
                id = UUID.randomUUID().toString(),
                senderId = args.userId,
                receiverId = "current_user_id",
                encryptedContent = "That's good to hear. Listen, I need a favor.",
                timestamp = currentTime - 20 * minuteMillis,
                isRead = true
            ))
            
            // Add a message with potential phishing content
            if (args.userId == "user2") {
                add(Message(
                    id = UUID.randomUUID().toString(),
                    senderId = args.userId,
                    receiverId = "current_user_id",
                    encryptedContent = "I found this amazing deal! Click on www.totallylegit.com to claim your free gift card now! You just need to enter your credit card details for verification.",
                    timestamp = currentTime - 15 * minuteMillis,
                    isRead = true,
                    isPhishingDetected = true,
                    phishingConfidence = 0.92f
                ))
                
                // Show warning banner
                binding.cardViewSecurityWarning.visibility = View.VISIBLE
            }
            
            // Add more recent messages
            add(Message(
                id = UUID.randomUUID().toString(),
                senderId = "current_user_id",
                receiverId = args.userId,
                encryptedContent = "What do you need?",
                timestamp = currentTime - 10 * minuteMillis,
                isRead = true
            ))
            
            add(Message(
                id = UUID.randomUUID().toString(),
                senderId = args.userId,
                receiverId = "current_user_id",
                encryptedContent = "Can we meet tomorrow to discuss that project?",
                timestamp = currentTime - 5 * minuteMillis,
                isRead = false
            ))
        }
        
        // Update adapter with messages
        adapter.submitList(messages)
        binding.recyclerViewMessages.scrollToPosition(messages.size - 1)
        binding.progressBarChat.visibility = View.GONE
    }
    
    private fun sendMessage(messageText: String) {
        // Check for potential phishing (demo for faculty presentation)
        val containsPhishing = messageText.contains("password") || 
                              messageText.contains("bank") || 
                              messageText.contains("click") || 
                              messageText.contains("urgent") ||
                              messageText.contains("send money")
        
        // Create and add a new message
        val newMessage = Message(
            id = UUID.randomUUID().toString(),
            senderId = "current_user_id",
            receiverId = args.userId,
            encryptedContent = messageText, // In a real app, this would be encrypted
            timestamp = System.currentTimeMillis(),
            isRead = false,
            isPhishingDetected = containsPhishing,
            phishingConfidence = if (containsPhishing) 0.85f else 0.0f,
            isSelfDestruct = isSelfDestructEnabled,
            selfDestructTime = if (isSelfDestructEnabled) System.currentTimeMillis() + 5 * 60 * 1000 else null // 5 minutes
        )
        
        // Add to list and update adapter
        val updatedList = messages.toMutableList().apply { add(newMessage) }
        messages.add(newMessage)
        adapter.submitList(updatedList)
        binding.recyclerViewMessages.scrollToPosition(messages.size - 1)
        
        // If phishing is detected in our message, show a toast for demo
        if (containsPhishing) {
            Toast.makeText(requireContext(), 
                "Warning: Your message contains content that could be a security risk.", 
                Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    // Adapter for chat messages
    private class MessagesAdapter(
        private val context: android.content.Context,
        private val currentUserId: String
    ) : androidx.recyclerview.widget.ListAdapter<Message, androidx.recyclerview.widget.RecyclerView.ViewHolder>(
        object : androidx.recyclerview.widget.DiffUtil.ItemCallback<Message>() {
            override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
                return oldItem.id == newItem.id
            }
            
            override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
                return oldItem == newItem
            }
        }
    ) {
        
        private val VIEW_TYPE_SENT = 1
        private val VIEW_TYPE_RECEIVED = 2
        
        override fun getItemViewType(position: Int): Int {
            val message = getItem(position)
            return if (message.senderId == currentUserId) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
            return when (viewType) {
                VIEW_TYPE_SENT -> {
                    val binding = com.example.sgp.databinding.ItemMessageSentBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                    SentMessageViewHolder(binding)
                }
                else -> {
                    val binding = com.example.sgp.databinding.ItemMessageReceivedBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                    ReceivedMessageViewHolder(binding)
                }
            }
        }
        
        override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
            val message = getItem(position)
            
            when (holder) {
                is SentMessageViewHolder -> holder.bind(message)
                is ReceivedMessageViewHolder -> holder.bind(message)
            }
        }
        
        inner class SentMessageViewHolder(private val binding: com.example.sgp.databinding.ItemMessageSentBinding) : 
            androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
                
            fun bind(message: Message) {
                // In a real app, message content would be decrypted here
                binding.textViewSentMessage.text = message.encryptedContent
                
                // Format timestamp
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                binding.textViewSentMessageTime.text = sdf.format(Date(message.timestamp))
                
                // Show self-destruct icon if needed
                binding.imageViewSelfDestruct.visibility = if (message.isSelfDestruct) View.VISIBLE else View.GONE
            }
        }
        
        inner class ReceivedMessageViewHolder(private val binding: com.example.sgp.databinding.ItemMessageReceivedBinding) : 
            androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {
                
            fun bind(message: Message) {
                // In a real app, message content would be decrypted here
                binding.textViewReceivedMessage.text = message.encryptedContent
                
                // Format timestamp
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                binding.textViewReceivedMessageTime.text = sdf.format(Date(message.timestamp))
                
                // Show phishing warning if needed
                binding.cardViewPhishingWarning.visibility = if (message.isPhishingDetected) View.VISIBLE else View.GONE
                
                // Show self-destruct icon if needed
                binding.imageViewReceivedSelfDestruct.visibility = if (message.isSelfDestruct) View.VISIBLE else View.GONE
            }
        }
    }
}
