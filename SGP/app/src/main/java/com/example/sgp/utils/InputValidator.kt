package com.example.sgp.utils

import android.util.Patterns
import java.util.regex.Pattern

object InputValidator {

    private val EMAIL_PATTERN = Patterns.EMAIL_ADDRESS
    private val PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$"
    )

    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && EMAIL_PATTERN.matcher(email).matches()
    }

    fun isValidPassword(password: String): Boolean {
        return password.isNotBlank() && PASSWORD_PATTERN.matcher(password).matches()
    }

    fun isValidDisplayName(displayName: String): Boolean {
        return displayName.isNotBlank() &&
               displayName.length >= 2 &&
               displayName.length <= 50 &&
               displayName.matches(Regex("^[a-zA-Z0-9\\s._-]+$"))
    }

    fun sanitizeMessage(message: String): String {
        return message.trim()
            .replace(Regex("<[^>]*>"), "") // Remove HTML tags
            .replace(Regex("\\s+"), " ") // Normalize whitespace
            .take(1000) // Limit message length
    }

    fun isValidMessage(message: String): Boolean {
        val sanitized = sanitizeMessage(message)
        return sanitized.isNotBlank() && sanitized.length <= 1000
    }

    fun containsSuspiciousPatterns(input: String): Boolean {
        val suspiciousPatterns = listOf(
            "javascript:",
            "<script",
            "eval(",
            "onclick=",
            "onerror=",
            "data:text/html"
        )

        val lowerInput = input.lowercase()
        return suspiciousPatterns.any { lowerInput.contains(it) }
    }

    fun validateAndSanitizeInput(input: String): ValidationResult {
        val trimmed = input.trim()

        return when {
            trimmed.isBlank() -> ValidationResult(false, "Input cannot be empty")
            containsSuspiciousPatterns(trimmed) -> ValidationResult(false, "Input contains suspicious content")
            trimmed.length > 1000 -> ValidationResult(false, "Input too long")
            else -> ValidationResult(true, sanitizeMessage(trimmed))
        }
    }

    data class ValidationResult(
        val isValid: Boolean,
        val result: String
    )
}
