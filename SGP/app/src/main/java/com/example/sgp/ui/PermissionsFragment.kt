package com.example.sgp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.sgp.MainActivity
import com.example.sgp.R
import com.example.sgp.databinding.FragmentPermissionsBinding

class PermissionsFragment : Fragment() {

    private var _binding: FragmentPermissionsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPermissionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPermissionButtons()
        updatePermissionStatuses()

        binding.btnContinueToDashboard.setOnClickListener {
            findNavController().navigate(R.id.dashboardFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        // Update status indicators when returning to the fragment
        updatePermissionStatuses()
    }

    private fun setupPermissionButtons() {
        binding.btnEnableNotification.setOnClickListener {
            (requireActivity() as MainActivity).openNotificationListenerSettings()
        }

        binding.btnEnableAccessibility.setOnClickListener {
            (requireActivity() as MainActivity).openAccessibilitySettings()
        }
    }

    private fun updatePermissionStatuses() {
        val mainActivity = requireActivity() as MainActivity

        // Update notification permission status
        if (mainActivity.isNotificationListenerEnabled()) {
            binding.ivNotificationStatus.setImageResource(R.drawable.ic_check_circle)
            binding.ivNotificationStatus.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.safe_green)
            )
            binding.tvNotificationStatus.text = "Enabled"
            binding.tvNotificationStatus.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.safe_green)
            )
        } else {
            binding.ivNotificationStatus.setImageResource(R.drawable.ic_security_warning)
            binding.ivNotificationStatus.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.caution_amber)
            )
            binding.tvNotificationStatus.text = "Not Enabled"
            binding.tvNotificationStatus.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.caution_amber)
            )
        }

        // Update accessibility permission status
        if (mainActivity.isAccessibilityServiceEnabled()) {
            binding.ivAccessibilityStatus.setImageResource(R.drawable.ic_check_circle)
            binding.ivAccessibilityStatus.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.safe_green)
            )
            binding.tvAccessibilityStatus.text = "Enabled"
            binding.tvAccessibilityStatus.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.safe_green)
            )
        } else {
            binding.ivAccessibilityStatus.setImageResource(R.drawable.ic_security_warning)
            binding.ivAccessibilityStatus.setColorFilter(
                ContextCompat.getColor(requireContext(), R.color.caution_amber)
            )
            binding.tvAccessibilityStatus.text = "Not Enabled"
            binding.tvAccessibilityStatus.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.caution_amber)
            )
        }

        // Enable or disable the continue button based on permissions
        binding.btnContinueToDashboard.isEnabled = mainActivity.isNotificationListenerEnabled() ||
                                                 mainActivity.isAccessibilityServiceEnabled()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
