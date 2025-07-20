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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.sgp.model.User
import java.util.UUID

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

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

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

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

        // Simple validation
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(requireContext(), "Passwords don't match", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(requireContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        // Show progress bar
        binding.progressBarRegister.visibility = View.VISIBLE

        // For demo purposes, simulate registration success
        if (email == "demo@example.com") {
            // Create a demo user
            val userId = UUID.randomUUID().toString()
            val user = User(
                uid = userId,
                displayName = name,
                email = email,
                lastSeen = System.currentTimeMillis()
            )

            // Store user in mock database
            binding.progressBarRegister.visibility = View.GONE
            Toast.makeText(requireContext(), "Account created successfully", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.conversationsFragment)
            return
        }

        // In a real app, register with Firebase
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: UUID.randomUUID().toString()

                    // Create user profile
                    val user = User(
                        uid = userId,
                        displayName = name,
                        email = email,
                        lastSeen = System.currentTimeMillis()
                    )

                    // Save to Firestore
                    db.collection("users")
                        .document(userId)
                        .set(user)
                        .addOnSuccessListener {
                            binding.progressBarRegister.visibility = View.GONE
                            Toast.makeText(requireContext(), "Account created successfully", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.conversationsFragment)
                        }
                        .addOnFailureListener { e ->
                            binding.progressBarRegister.visibility = View.GONE
                            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    binding.progressBarRegister.visibility = View.GONE
                    Toast.makeText(requireContext(), "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
