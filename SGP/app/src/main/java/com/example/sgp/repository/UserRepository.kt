package com.example.sgp.repository

import androidx.lifecycle.LiveData
import com.example.sgp.data.SecureChatDatabase
import com.example.sgp.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(private val database: SecureChatDatabase) {

    fun getAllUsers(): LiveData<List<User>> {
        return database.userDao().getAllUsers()
    }

    suspend fun insertUser(user: User) {
        withContext(Dispatchers.IO) {
            database.userDao().insertUser(user)
        }
    }

    suspend fun updateUser(user: User) {
        withContext(Dispatchers.IO) {
            database.userDao().updateUser(user)
        }
    }

    suspend fun deleteUser(uid: String) {
        withContext(Dispatchers.IO) {
            database.userDao().deleteUser(uid)
        }
    }

    suspend fun getUserById(uid: String): User? {
        return withContext(Dispatchers.IO) {
            database.userDao().getUserById(uid)
        }
    }

    suspend fun getUserByEmail(email: String): User? {
        return withContext(Dispatchers.IO) {
            database.userDao().getUserByEmail(email)
        }
    }

    suspend fun updateUserOnlineStatus(uid: String, isOnline: Boolean) {
        withContext(Dispatchers.IO) {
            database.userDao().updateOnlineStatus(uid, isOnline)
        }
    }

    suspend fun updateLastSeen(uid: String, timestamp: Long) {
        withContext(Dispatchers.IO) {
            database.userDao().updateLastSeen(uid, timestamp)
        }
    }

    suspend fun searchUsers(query: String): List<User> {
        return withContext(Dispatchers.IO) {
            database.userDao().searchUsers(query)
        }
    }
}
