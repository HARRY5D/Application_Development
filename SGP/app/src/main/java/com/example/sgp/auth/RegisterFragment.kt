package com.example.sgp.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.sgp.R
import com.example.sgp.databinding.FragmentRegisterBinding
import com.example.sgp.utils.InputValidator
import com.example.sgp.model.User
import java.util.UUID

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up click listeners
        binding.btnRegister.setOnClickListener {
            registerUser()
        }

        binding.tvBackToLogin.setOnClickListener {
            findNavController().navigate(R.id.loginFragment)
        }
    }

    private fun registerUser() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etRegisterEmail.text.toString().trim()
        val password = binding.etRegisterPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        // Validation using InputValidator
        if (!InputValidator.isValidDisplayName(name)) {
            Toast.makeText(requireContext(), "Please enter a valid name", Toast.LENGTH_SHORT).show()
            return
        }

        if (!InputValidator.isValidEmail(email)) {
            Toast.makeText(requireContext(), "Please enter a valid email", Toast.LENGTH_SHORT).show()
            return
        }

        if (!InputValidator.isValidPassword(password)) {
            Toast.makeText(requireContext(), "Password must be at least 8 characters with uppercase, lowercase, number and special character", Toast.LENGTH_LONG).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(requireContext(), "Passwords don't match", Toast.LENGTH_SHORT).show()
            return
        }

        // Show progress bar
        binding.progressBarRegister.visibility = View.VISIBLE

        // Local registration - for demo purposes
        simulateLocalRegistration(name, email, password)
    }

    private fun simulateLocalRegistration(name: String, email: String, password: String) {
        // Simulate registration delay
        binding.root.postDelayed({
            binding.progressBarRegister.visibility = View.GONE

            // Create user object for local storage
            val user = User(
                uid = UUID.randomUUID().toString(),
                displayName = name,
                email = email,
                photoUrl = null,
                publicKey = null,
                lastSeen = System.currentTimeMillis(),
                isOnline = true
            )

            // In a real app, you would save user to local database
            Toast.makeText(requireContext(), "Registration successful! Welcome $name", Toast.LENGTH_SHORT).show()

            // Navigate back to login
            findNavController().navigate(R.id.loginFragment)
        }, 1500) // 1.5 second delay for demo
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
