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

    @Query("SELECT * FROM users WHERE uid = :userId")
    fun getUserById(userId: String): LiveData<User>

    @Query("SELECT * FROM users WHERE uid = :userId")
    suspend fun getUserByIdSync(userId: String): User?

    @Query("SELECT * FROM users")
    fun getAllUsers(): LiveData<List<User>>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?
}
