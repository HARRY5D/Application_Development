package com.example.sgp.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.sgp.R
import com.example.sgp.databinding.FragmentLoginBinding
import com.example.sgp.utils.InputValidator

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up click listeners
        binding.btnLogin.setOnClickListener {
            loginUser()
        }

        binding.tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }
    }

    private fun loginUser() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // Simple validation using our InputValidator
        if (!InputValidator.isValidEmail(email)) {
            Toast.makeText(requireContext(), "Please enter a valid email", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter password", Toast.LENGTH_SHORT).show()
            return
        }

        // Show progress bar
        binding.progressBar.visibility = View.VISIBLE

        // Local authentication - for demo purposes, accept any valid email format
        // In a real app, you would check against local database
        simulateLocalLogin(email, password)
    }

    private fun simulateLocalLogin(email: String, password: String) {
        // Simulate authentication delay
        binding.root.postDelayed({
            binding.progressBar.visibility = View.GONE

            // For demo - accept any valid email and non-empty password
            if (InputValidator.isValidEmail(email) && password.length >= 6) {
                Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show()
                // Navigate to conversations screen
                findNavController().navigate(R.id.conversationsFragment)
            } else {
                Toast.makeText(requireContext(), "Invalid credentials", Toast.LENGTH_SHORT).show()
            }
        }, 1000) // 1 second delay for demo
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
