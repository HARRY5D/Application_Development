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
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

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

        auth = FirebaseAuth.getInstance()

        // Check if user is already logged in
        if (auth.currentUser != null) {
            // Navigate to conversations screen
            findNavController().navigate(R.id.conversationsFragment)
        }

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

        // Simple validation
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Show progress bar
        binding.progressBar.visibility = View.VISIBLE

        // For demo purposes, use a mock login instead of actual Firebase auth
        // In a real app, you would use Firebase Authentication
        if (email == "demo@example.com" && password == "password") {
            // Navigate to conversations screen
            binding.progressBar.visibility = View.GONE
            findNavController().navigate(R.id.conversationsFragment)
        } else {
            // Simulate Firebase authentication (for demo)
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    binding.progressBar.visibility = View.GONE
                    if (task.isSuccessful) {
                        // Navigate to conversations screen
                        findNavController().navigate(R.id.conversationsFragment)
                    } else {
                        Toast.makeText(requireContext(),
                            "Authentication failed: ${task.exception?.message ?: "Unknown error"}",
                            Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
