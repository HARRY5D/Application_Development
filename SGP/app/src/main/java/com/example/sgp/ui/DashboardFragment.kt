package com.example.sgp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sgp.MainActivity
import com.example.sgp.R
import com.example.sgp.databinding.FragmentDashboardBinding
import com.example.sgp.model.ThreatItem
import com.example.sgp.repository.ThreatRepository
import com.example.sgp.ui.adapter.ThreatAdapter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var threatAdapter: ThreatAdapter
    private lateinit var threatRepository: ThreatRepository

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

        threatRepository = ThreatRepository.getInstance()
        setupRecyclerView()
        updateSystemStatus()
        observeThreatData()
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
    }

    private fun observeThreatData() {
        threatRepository.threatItems.observe(viewLifecycleOwner) { threats ->
            threatAdapter.submitList(threats)
            updateEmptyState(threats.isEmpty())
            updateStatistics()
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.emptyStateView.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvThreatList.visibility = if (isEmpty) View.GONE else View.VISIBLE
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

    private fun updateStatistics() {
        // Update the stats counters (just for demonstration)
        binding.tvTotalScanned.text = "42"
        binding.tvThreatsDetected.text = threatRepository.getThreatCount().toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
