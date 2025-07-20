package com.example.sgp.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sgp.R
import com.example.sgp.databinding.FragmentConversationsBinding
import com.example.sgp.model.Conversation
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ConversationsFragment : Fragment() {

    private var _binding: FragmentConversationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ConversationsAdapter
    private val conversations = mutableListOf<Conversation>()

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

        setupToolbar()
        setupRecyclerView()
        loadMockConversations()

        binding.fabNewChat.setOnClickListener {
            // In a real app, this would show a user selection dialog
            Toast.makeText(requireContext(), "New chat feature coming soon!", Toast.LENGTH_SHORT).show()

            // For demo purposes, navigate to a mock chat
            findNavController().navigate(R.id.chatFragment, Bundle().apply {
                putString("userId", "demo_user")
                putString("userName", "Demo User")
            })
        }
    }

    private fun setupToolbar() {
        binding.toolbar.inflateMenu(R.menu.menu_conversations)
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_logout -> {
                    // In a real app, sign out from Firebase Auth
                    findNavController().navigate(R.id.loginFragment)
                    true
                }
                R.id.action_settings -> {
                    Toast.makeText(requireContext(), "Settings coming soon!", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = ConversationsAdapter { conversation ->
            // Navigate to chat screen when a conversation is clicked
            findNavController().navigate(R.id.chatFragment, Bundle().apply {
                putString("userId", conversation.participantIds[0])
                putString("userName", conversation.groupName ?: "User")
            })
        }

        binding.recyclerViewConversations.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ConversationsFragment.adapter
        }
    }

    private fun loadMockConversations() {
        binding.progressBarConversations.visibility = View.VISIBLE

        // For demo purposes, create some mock conversations
        val currentTime = System.currentTimeMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000L

        conversations.apply {
            clear()
            add(Conversation(
                id = "conv1",
                participantIds = listOf("user1"),
                lastMessagePreview = "Hey, how are you doing?",
                lastMessageTime = currentTime,
                unreadCount = 2,
                groupName = "XYZ"
            ))
            add(Conversation(
                id = "conv2",
                participantIds = listOf("user2"),
                lastMessagePreview = "Click this link to claim your prize: www.totallylegit.com",
                lastMessageTime = currentTime - oneDayMillis,
                unreadCount = 0,
                groupName = "Alice Smith"
            ))
            add(Conversation(
                id = "conv3",
                participantIds = listOf("user3", "user4", "user5"),
                lastMessagePreview = "Let's meet tomorrow at 10",
                lastMessageTime = currentTime - 2 * oneDayMillis,
                unreadCount = 0,
                isGroup = true,
                groupName = "Project Team"
            ))
            add(Conversation(
                id = "conv4",
                participantIds = listOf("user6"),
                lastMessagePreview = "I need to ask you something important",
                lastMessageTime = currentTime - 3 * oneDayMillis,
                unreadCount = 1,
                groupName = "Bob Johnson"
            ))
        }

        // Show empty state if no conversations
        binding.tvNoConversations.visibility = if (conversations.isEmpty()) View.VISIBLE else View.GONE

        // Update the adapter
        adapter.submitList(conversations)
        binding.progressBarConversations.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Inner class for the adapter
    inner class ConversationsAdapter(
        private val onConversationClicked: (Conversation) -> Unit
    ) : androidx.recyclerview.widget.ListAdapter<Conversation, ConversationsAdapter.ConversationViewHolder>(
        object : androidx.recyclerview.widget.DiffUtil.ItemCallback<Conversation>() {
            override fun areItemsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
                return oldItem == newItem
            }
        }
    ) {

        inner class ConversationViewHolder(val binding: com.example.sgp.databinding.ItemConversationBinding) :
            androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
            val binding = com.example.sgp.databinding.ItemConversationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ConversationViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
            val conversation = getItem(position)

            holder.binding.apply {
                // Set conversation name
                textViewName.text = conversation.groupName

                // Set last message preview
                textViewLastMessage.text = conversation.lastMessagePreview

                // Format and set time
                val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                textViewTime.text = sdf.format(Date(conversation.lastMessageTime))

                // Set unread count
                if (conversation.unreadCount > 0) {
                    textViewUnreadCount.visibility = View.VISIBLE
                    textViewUnreadCount.text = if (conversation.unreadCount > 9) "9+" else conversation.unreadCount.toString()
                } else {
                    textViewUnreadCount.visibility = View.GONE
                }

                // Click listener
                root.setOnClickListener {
                    onConversationClicked(conversation)
                }
            }
        }
    }
}
