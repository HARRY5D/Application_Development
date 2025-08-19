package com.example.campus_lost_found

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.campus_lost_found.utils.SupabaseManager
import com.google.android.gms.common.SignInButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private val TAG = "LoginActivity"

    // UI elements
    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var signupButton: Button
    private lateinit var forgotPasswordText: TextView
    private lateinit var useDefaultEmailText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var googleSignInButton: SignInButton

    // Default credentials for testing
    private val defaultEmail = "test@example.com"
    private val defaultPassword = "test123456"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            setTheme(R.style.Theme_CAMPUS_LOST_FOUND)
            setContentView(R.layout.activity_login)

            // Check if user is already signed in with Supabase
            if (SupabaseManager.getInstance().isLoggedIn()) {
                startMainActivity()
                return
            }

            initializeViews()
            setupListeners()

        } catch (e: Exception) {
            Log.e(TAG, "Critical error during initialization: ${e.message}")
            // If anything fails, just go to main activity
            startMainActivity()
        }
    }

    private fun initializeViews() {
        try {
            emailLayout = findViewById(R.id.emailLayout)
            passwordLayout = findViewById(R.id.passwordLayout)
            emailInput = findViewById(R.id.emailInput)
            passwordInput = findViewById(R.id.passwordInput)
            loginButton = findViewById(R.id.loginButton)
            signupButton = findViewById(R.id.signupButton)
            forgotPasswordText = findViewById(R.id.forgotPasswordText)
            useDefaultEmailText = findViewById(R.id.useDefaultEmailText)
            progressBar = findViewById(R.id.progressBar)
            googleSignInButton = findViewById(R.id.googleSignInButton)
        } catch (e: Exception) {
            Log.e(TAG, "View initialization failed: ${e.message}")
            throw e
        }
    }

    private fun setupListeners() {
        // Google OAuth Sign-In (will implement Supabase OAuth later)
        googleSignInButton.setOnClickListener {
            showComingSoonDialog("Google OAuth with Supabase will be available soon!\nFor now, please use email/password login below.")
        }

        // Default email login
        useDefaultEmailText.setOnClickListener {
            emailInput.setText(defaultEmail)
            passwordInput.setText(defaultPassword)
            Toast.makeText(this, "Default credentials filled. You can modify them or click Login.", Toast.LENGTH_SHORT).show()
        }

        // Regular login button
        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (validateLoginInputs(email, password)) {
                signInWithEmailPassword(email, password)
            }
        }

        // Sign up button
        signupButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (validateSignupInputs(email, password)) {
                signUpWithEmailPassword(email, password)
            }
        }

        // Forgot password
        forgotPasswordText.setOnClickListener {
            showComingSoonDialog("Password reset with Supabase will be available soon!")
        }
    }

    private fun validateLoginInputs(email: String, password: String): Boolean {
        var isValid = true

        if (email.isEmpty()) {
            emailLayout.error = "Email is required"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.error = "Please enter a valid email"
            isValid = false
        } else {
            emailLayout.error = null
        }

        if (password.isEmpty()) {
            passwordLayout.error = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            passwordLayout.error = "Password must be at least 6 characters"
            isValid = false
        } else {
            passwordLayout.error = null
        }

        return isValid
    }

    private fun validateSignupInputs(email: String, password: String): Boolean {
        var isValid = true

        if (email.isEmpty()) {
            emailLayout.error = "Email is required"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.error = "Please enter a valid email"
            isValid = false
        } else {
            emailLayout.error = null
        }

        if (password.isEmpty()) {
            passwordLayout.error = "Password is required"
            isValid = false
        } else if (password.length < 8) {
            passwordLayout.error = "Password must be at least 8 characters"
            isValid = false
        } else {
            passwordLayout.error = null
        }

        return isValid
    }

    private fun signInWithEmailPassword(email: String, password: String) {
        showProgress(true)

        lifecycleScope.launch {
            try {
                val result = SupabaseManager.getInstance().signInWithEmail(email, password)

                if (result.isSuccess) {
                    val message = result.getOrNull() ?: "Sign in successful!"
                    Toast.makeText(this@LoginActivity, message, Toast.LENGTH_SHORT).show()
                    startMainActivity()
                } else {
                    val error = result.exceptionOrNull()
                    showError("Sign in failed: ${error?.message ?: "Unknown error"}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Sign in error: ${e.message}")
                showError("Sign in failed: ${e.message}")
            } finally {
                showProgress(false)
            }
        }
    }

    private fun signUpWithEmailPassword(email: String, password: String) {
        showProgress(true)

        lifecycleScope.launch {
            try {
                val result = SupabaseManager.getInstance().signUpWithEmail(email, password)

                if (result.isSuccess) {
                    val message = result.getOrNull() ?: "Account created successfully!"
                    showSuccess(message)
                } else {
                    val error = result.exceptionOrNull()
                    showError("Sign up failed: ${error?.message ?: "Unknown error"}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Sign up error: ${e.message}")
                showError("Sign up failed: ${e.message}")
            } finally {
                showProgress(false)
            }
        }
    }

    private fun showProgress(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        loginButton.isEnabled = !show
        signupButton.isEnabled = !show
        googleSignInButton.isEnabled = !show
    }

    private fun showError(message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showSuccess(message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Success")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showComingSoonDialog(message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Coming Soon")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
