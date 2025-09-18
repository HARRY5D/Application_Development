package com.example.sgp.ml

import java.util.regex.Pattern

class ThreatDetector {

    companion object {
        // Common phishing indicators
        private val PHISHING_PATTERNS = listOf(
            Pattern.compile("(click here|verify account|suspended|urgent|immediate)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(winner|congratulations|lottery|prize)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(bank|paypal|amazon|microsoft|google).*(verify|confirm|update)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(http://|https://)[^\\s]+", Pattern.CASE_INSENSITIVE) // URLs
        )

        // Spam indicators
        private val SPAM_PATTERNS = listOf(
            Pattern.compile("(free|offer|discount|sale|buy now)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(limited time|act now|don't miss)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\$[0-9,]+", Pattern.CASE_INSENSITIVE) // Money amounts
        )

        // Abusive content indicators
        private val ABUSIVE_PATTERNS = listOf(
            Pattern.compile("(hate|kill|die|stupid|idiot)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(threat|harm|hurt|violence)", Pattern.CASE_INSENSITIVE)
        )
    }

    fun analyzePatterns(messages: List<String>): PatternAnalysis {
        var phishingCount = 0
        var spamCount = 0
        var abusiveCount = 0
        val suspiciousMessages = mutableListOf<SuspiciousMessage>()

        messages.forEachIndexed { index, message ->
            val phishingScore = calculatePatternScore(message, PHISHING_PATTERNS)
            val spamScore = calculatePatternScore(message, SPAM_PATTERNS)
            val abusiveScore = calculatePatternScore(message, ABUSIVE_PATTERNS)

            if (phishingScore > 0.3f) {
                phishingCount++
                suspiciousMessages.add(
                    SuspiciousMessage(index, message, "phishing", phishingScore)
                )
            }

            if (spamScore > 0.3f) {
                spamCount++
                suspiciousMessages.add(
                    SuspiciousMessage(index, message, "spam", spamScore)
                )
            }

            if (abusiveScore > 0.3f) {
                abusiveCount++
                suspiciousMessages.add(
                    SuspiciousMessage(index, message, "abusive", abusiveScore)
                )
            }
        }

        return PatternAnalysis(
            totalMessages = messages.size,
            phishingAttempts = phishingCount,
            spamMessages = spamCount,
            abusiveContent = abusiveCount,
            suspiciousMessages = suspiciousMessages,
            overallRiskScore = calculateOverallRisk(messages.size, phishingCount, spamCount, abusiveCount)
        )
    }

    private fun calculatePatternScore(message: String, patterns: List<Pattern>): Float {
        var score = 0f
        var matchCount = 0

        patterns.forEach { pattern ->
            if (pattern.matcher(message).find()) {
                matchCount++
                score += 1f / patterns.size
            }
        }

        // Boost score if multiple patterns match
        if (matchCount > 1) {
            score *= 1.5f
        }

        return score.coerceAtMost(1.0f)
    }

    private fun calculateOverallRisk(total: Int, phishing: Int, spam: Int, abusive: Int): Float {
        if (total == 0) return 0f

        val threatRatio = (phishing + spam + abusive).toFloat() / total
        return when {
            threatRatio > 0.5f -> 0.9f // High risk
            threatRatio > 0.3f -> 0.7f // Medium risk
            threatRatio > 0.1f -> 0.4f // Low risk
            else -> 0.1f // Very low risk
        }
    }

    fun detectSocialEngineering(message: String): SocialEngineeringAnalysis {
        val urgencyScore = detectUrgency(message)
        val authorityScore = detectAuthority(message)
        val emotionalScore = detectEmotionalManipulation(message)
        val scarcityScore = detectScarcity(message)

        val overallScore = (urgencyScore + authorityScore + emotionalScore + scarcityScore) / 4f

        return SocialEngineeringAnalysis(
            urgencyScore = urgencyScore,
            authorityScore = authorityScore,
            emotionalScore = emotionalScore,
            scarcityScore = scarcityScore,
            overallScore = overallScore,
            isSocialEngineering = overallScore > 0.6f
        )
    }

    private fun detectUrgency(message: String): Float {
        val urgencyWords = listOf("urgent", "immediate", "asap", "hurry", "quickly", "now", "today")
        return calculateWordScore(message, urgencyWords)
    }

    private fun detectAuthority(message: String): Float {
        val authorityWords = listOf("bank", "government", "police", "security", "official", "department")
        return calculateWordScore(message, authorityWords)
    }

    private fun detectEmotionalManipulation(message: String): Float {
        val emotionalWords = listOf("congratulations", "winner", "lucky", "fear", "worried", "concerned")
        return calculateWordScore(message, emotionalWords)
    }

    private fun detectScarcity(message: String): Float {
        val scarcityWords = listOf("limited", "exclusive", "only", "last chance", "expires", "deadline")
        return calculateWordScore(message, scarcityWords)
    }

    private fun calculateWordScore(message: String, words: List<String>): Float {
        val lowerMessage = message.lowercase()
        val matchCount = words.count { lowerMessage.contains(it) }
        return (matchCount.toFloat() / words.size).coerceAtMost(1.0f)
    }

    data class PatternAnalysis(
        val totalMessages: Int,
        val phishingAttempts: Int,
        val spamMessages: Int,
        val abusiveContent: Int,
        val suspiciousMessages: List<SuspiciousMessage>,
        val overallRiskScore: Float
    )

    data class SuspiciousMessage(
        val index: Int,
        val content: String,
        val threatType: String,
        val score: Float
    )

    data class SocialEngineeringAnalysis(
        val urgencyScore: Float,
        val authorityScore: Float,
        val emotionalScore: Float,
        val scarcityScore: Float,
        val overallScore: Float,
        val isSocialEngineering: Boolean
    )
}
