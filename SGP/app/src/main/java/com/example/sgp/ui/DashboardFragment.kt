package com.example.sgp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sgp.MainActivity
import com.example.sgp.R
import com.example.sgp.databinding.FragmentDashboardBinding
import com.example.sgp.model.ThreatItem
import com.example.sgp.ui.adapter.ThreatAdapter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var threatAdapter: ThreatAdapter
    private val threatItems = mutableListOf<ThreatItem>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        updateSystemStatus()
        loadDemoThreats() // Load some demo threats for demonstration
        updateStatistics()
    }

    override fun onResume() {
        super.onResume()
        // Update status indicators when returning to the fragment
        updateSystemStatus()
    }

    private fun setupRecyclerView() {
        threatAdapter = ThreatAdapter()
        binding.rvThreatList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = threatAdapter
        }

        // Show empty state if no threats
        if (threatItems.isEmpty()) {
            binding.emptyStateView.visibility = View.VISIBLE
        } else {
            binding.emptyStateView.visibility = View.GONE
        }
    }

    private fun updateSystemStatus() {
        val mainActivity = requireActivity() as MainActivity

        // Update notification monitoring status
        if (mainActivity.isNotificationListenerEnabled()) {
            binding.ivNotificationStatus.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.safe_green)
            )
            binding.tvNotificationMonitorStatus.text = "ACTIVE"
            binding.tvNotificationMonitorStatus.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.safe_green)
            )
        } else {
            binding.ivNotificationStatus.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.caution_amber)
            )
            binding.tvNotificationMonitorStatus.text = "INACTIVE"
            binding.tvNotificationMonitorStatus.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.caution_amber)
            )
        }

        // Update screen monitoring status
        if (mainActivity.isAccessibilityServiceEnabled()) {
            binding.ivAccessibilityStatus.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.safe_green)
            )
            binding.tvScreenMonitorStatus.text = "ACTIVE"
            binding.tvScreenMonitorStatus.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.safe_green)
            )
        } else {
            binding.ivAccessibilityStatus.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.caution_amber)
            )
            binding.tvScreenMonitorStatus.text = "INACTIVE"
            binding.tvScreenMonitorStatus.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.caution_amber)
            )
        }

        // ML Model status (this is just for demonstration)
        binding.ivMlModelStatus.setColorFilter(
            ContextCompat.getColor(requireContext(), R.color.safe_green)
        )
        binding.tvMlModelStatus.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.safe_green)
        )
    }

    private fun loadDemoThreats() {
        // For demonstration purposes, add some example threats
        val demoThreats = listOf(
            ThreatItem(
                id = 1,
                messageContent = "Congratulations! You've won a $500 Amazon gift card. Click here to claim: http://amaz0n-gift.tk",
                sender = "Unknown",
                sourceApp = "WhatsApp",
                threatType = "PHISHING",
                confidenceScore = 92,
                timestamp = System.currentTimeMillis() - 15 * 60 * 1000 // 15 minutes ago
            ),
            ThreatItem(
                id = 2,
                messageContent = "Your account has been compromised. Please verify your identity by clicking: http://secur1ty-verify.com",
                sender = "Security Team",
                sourceApp = "SMS",
                threatType = "SMISHING",
                confidenceScore = 87,
                timestamp = System.currentTimeMillis() - 3 * 60 * 60 * 1000 // 3 hours ago
            ),
            ThreatItem(
                id = 3,
                messageContent = "Urgent: Your payment failed. Update your billing info: http://paym3nt-update.xyz",
                sender = "Netflix",
                sourceApp = "Gmail",
                threatType = "PHISHING",
                confidenceScore = 78,
                timestamp = System.currentTimeMillis() - 24 * 60 * 60 * 1000 // 1 day ago
            )
        )

        threatItems.clear()
        threatItems.addAll(demoThreats)
        threatAdapter.submitList(threatItems)

        // Update empty state visibility
        binding.emptyStateView.visibility = if (threatItems.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun updateStatistics() {
        // Update the stats counters (just for demonstration)
        binding.tvTotalScanned.text = "42"
        binding.tvThreatsDetected.text = threatItems.size.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
