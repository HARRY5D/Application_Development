package com.example.sgp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val uid: String,
    val displayName: String,
    val email: String,
    val photoUrl: String? = null,
    val publicKey: String? = null,
    val lastSeen: Long = 0,
    val isOnline: Boolean = false
)
