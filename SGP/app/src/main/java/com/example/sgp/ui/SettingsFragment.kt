package com.example.sgp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.example.sgp.R
import com.example.sgp.databinding.FragmentSettingsBinding
import com.google.android.material.snackbar.Snackbar

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSeekBar()
        setupButtons()
    }

    private fun setupSeekBar() {
        binding.seekConfidenceThreshold.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.tvConfidenceThreshold.text = "$progress%"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Save the threshold value (for demonstration purposes we're not implementing actual saving)
                Snackbar.make(
                    binding.root,
                    "Threshold set to ${binding.seekConfidenceThreshold.progress}%",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun setupButtons() {
        binding.btnClearThreatData.setOnClickListener {
            // Show confirmation dialog (in a real app)
            Snackbar.make(
                binding.root,
                "Threat history cleared",
                Snackbar.LENGTH_SHORT
            ).show()
        }

        // App switch toggles
        binding.switchWhatsapp.setOnCheckedChangeListener { _, isChecked ->
            // In a real app, save this preference
            showToggleMessage("WhatsApp monitoring", isChecked)
        }

        binding.switchSms.setOnCheckedChangeListener { _, isChecked ->
            showToggleMessage("SMS monitoring", isChecked)
        }

        binding.switchTelegram.setOnCheckedChangeListener { _, isChecked ->
            showToggleMessage("Telegram monitoring", isChecked)
        }

        binding.switchGmail.setOnCheckedChangeListener { _, isChecked ->
            showToggleMessage("Gmail monitoring", isChecked)
        }

        binding.switchAlertNotifications.setOnCheckedChangeListener { _, isChecked ->
            showToggleMessage("Alert notifications", isChecked)
        }
    }

    private fun showToggleMessage(feature: String, isEnabled: Boolean) {
        val status = if (isEnabled) "enabled" else "disabled"
        Snackbar.make(
            binding.root,
            "$feature $status",
            Snackbar.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
