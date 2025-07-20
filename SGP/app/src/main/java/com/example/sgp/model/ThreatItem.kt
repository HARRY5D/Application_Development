package com.example.sgp.model

data class ThreatItem(
    val id: Long,
    val messageContent: String,
    val sender: String,
    val sourceApp: String,
    val threatType: String,
    val confidenceScore: Int,
    val timestamp: Long
)
