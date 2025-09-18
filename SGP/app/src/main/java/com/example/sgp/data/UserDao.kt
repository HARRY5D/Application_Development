package com.example.sgp.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.sgp.model.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

    @Query("DELETE FROM users WHERE uid = :uid")
    suspend fun deleteUser(uid: String)

    @Query("SELECT * FROM users WHERE uid = :userId")
    suspend fun getUserById(userId: String): User?

    @Query("SELECT * FROM users WHERE uid = :userId")
    fun getUserByIdLiveData(userId: String): LiveData<User>

    @Query("SELECT * FROM users WHERE uid = :userId")
    suspend fun getUserByIdSync(userId: String): User?

    @Query("SELECT * FROM users")
    fun getAllUsers(): LiveData<List<User>>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("UPDATE users SET isOnline = :isOnline WHERE uid = :uid")
    suspend fun updateOnlineStatus(uid: String, isOnline: Boolean)

    @Query("UPDATE users SET lastSeen = :timestamp WHERE uid = :uid")
    suspend fun updateLastSeen(uid: String, timestamp: Long)

    @Query("SELECT * FROM users WHERE displayName LIKE :query OR email LIKE :query")
    suspend fun searchUsers(query: String): List<User>
}
