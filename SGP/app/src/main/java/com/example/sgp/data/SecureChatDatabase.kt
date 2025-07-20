package com.example.sgp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.sgp.model.Conversation
import com.example.sgp.model.Message
import com.example.sgp.model.User

@Database(
    entities = [User::class, Message::class, Conversation::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SecureChatDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun messageDao(): MessageDao
    abstract fun conversationDao(): ConversationDao

    companion object {
        @Volatile
        private var INSTANCE: SecureChatDatabase? = null

        fun getInstance(context: Context): SecureChatDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SecureChatDatabase::class.java,
                    "secure_chat_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
