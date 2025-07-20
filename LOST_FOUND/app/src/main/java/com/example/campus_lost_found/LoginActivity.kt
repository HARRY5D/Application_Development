package com.example.campus_lost_found

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.SignInButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

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
    private lateinit var skipButton: Button

    // Default credentials
    private val defaultEmail = "sgp.noreplydce@gmail.com"
    private val defaultPassword = "campus123"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Force apply the dark theme
        setTheme(R.style.Theme_CAMPUS_LOST_FOUND)

        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Check if user is already signed in
        if (auth.currentUser != null) {
            startMainActivity()
            return
        }

        // Initialize views
        initializeViews()

        // Set up listeners
        setupListeners()
    }

    private fun initializeViews() {
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
        skipButton = findViewById(R.id.skipButton)
    }

    private fun setupListeners() {
        // Skip authentication button
        skipButton.setOnClickListener {
            // Skip authentication and continue to main activity
            Toast.makeText(this,
                "Proceeding without authentication",
                Toast.LENGTH_SHORT).show()
            startMainActivity()
        }

        // Google Sign-In button - temporarily showing message
        googleSignInButton.setOnClickListener {
            showGoogleSignInNotAvailableDialog()
        }

        // Default email login
        useDefaultEmailText.setOnClickListener {
            // Just go directly to main activity without authentication
            Toast.makeText(this, "Proceeding as default user", Toast.LENGTH_SHORT).show()
            startMainActivity()
        }

        // Regular login button
        loginButton.setOnClickListener {
            // Try to login, but if it fails, just go to main activity
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (!validateInput(email, password)) {
                return@setOnClickListener
            }

            setLoading(true)

            // Try to authenticate, but don't block the user if it fails
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    setLoading(false)
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Signed in successfully", Toast.LENGTH_SHORT).show()
                        startMainActivity()
                    } else {
                        // Authentication failed, but let them in anyway
                        Toast.makeText(this,
                            "Authentication bypassed: ${task.exception?.message}",
                            Toast.LENGTH_SHORT).show()
                        startMainActivity()
                    }
                }
        }

        // Create account button
        signupButton.setOnClickListener {
            // Try to create an account, but if it fails, just go to main activity
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (!validateInput(email, password)) {
                return@setOnClickListener
            }

            setLoading(true)

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    setLoading(false)
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this,
                            "Account creation bypassed: ${task.exception?.message}",
                            Toast.LENGTH_SHORT).show()
                    }
                    // Whether successful or not, continue to main activity
                    startMainActivity()
                }
        }

        // Forgot password link
        forgotPasswordText.setOnClickListener {
            // Just show a toast and continue to main activity
            Toast.makeText(this, "Password recovery bypassed. Continuing to app.",
                Toast.LENGTH_SHORT).show()
            startMainActivity()
        }
    }

    private fun showGoogleSignInNotAvailableDialog() {
        Toast.makeText(this, "Continuing without Google authentication", Toast.LENGTH_SHORT).show()
        startMainActivity()
    }

    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true

        // Validate email
        if (email.isEmpty()) {
            emailLayout.error = "Email cannot be empty"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.error = "Please enter a valid email"
            isValid = false
        } else {
            emailLayout.error = null
        }

        // Validate password
        if (password.isEmpty()) {
            passwordLayout.error = "Password cannot be empty"
            isValid = false
        } else if (password.length < 6) {
            passwordLayout.error = "Password must be at least 6 characters"
            isValid = false
        } else {
            passwordLayout.error = null
        }

        return isValid
    }

    private fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
            loginButton.isEnabled = false
            signupButton.isEnabled = false
            googleSignInButton.isEnabled = false
            useDefaultEmailText.isEnabled = false
            forgotPasswordText.isEnabled = false
            skipButton.isEnabled = false
        } else {
            progressBar.visibility = View.GONE
            loginButton.isEnabled = true
            signupButton.isEnabled = true
            googleSignInButton.isEnabled = true
            useDefaultEmailText.isEnabled = true
            forgotPasswordText.isEnabled = true
            skipButton.isEnabled = true
        }
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Close the login activity
    }
}
