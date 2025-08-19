package com.example.campus_lost_found.repository

import android.util.Log
import com.example.campus_lost_found.model.FoundItem
import com.example.campus_lost_found.model.LostItem
import com.example.campus_lost_found.utils.SupabaseManager
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import java.util.Date

@Serializable
data class SupabaseLostItem(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val lastSeenLocation: String = "",
    val imageUrl: String = "",
    val reportedBy: String = "",
    val reportedByName: String = "",
    val reportedDate: String = "",
    val dateLost: String = "",
    val found: Boolean = false,
    val foundBy: String = "",
    val foundByName: String = ""
)

@Serializable
data class SupabaseFoundItem(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val location: String = "",
    val keptAt: String = "",
    val imageUrl: String = "",
    val reportedBy: String = "",
    val reportedByName: String = "",
    val reportedDate: String = "",
    val dateFound: String = "",
    val claimed: Boolean = false,
    val claimedBy: String = "",
    val claimedByName: String = ""
)

class SupabaseRepository {
    private val supabase = SupabaseManager.getClient()

    suspend fun addLostItem(lostItem: LostItem): Result<String> = withContext(Dispatchers.IO) {
        try {
            val supabaseItem = SupabaseLostItem(
                id = lostItem.id,
                name = lostItem.name,
                description = lostItem.description,
                category = lostItem.category,
                lastSeenLocation = lostItem.location,
                imageUrl = lostItem.imageUrl,
                reportedBy = lostItem.reportedBy,
                reportedByName = lostItem.reportedByName,
                reportedDate = lostItem.reportedDate.toDate().time.toString(),
                dateLost = lostItem.dateLost.toDate().time.toString(),
                found = lostItem.found,
                foundBy = lostItem.foundBy,
                foundByName = lostItem.foundByName
            )

            supabase.from("lost_items").insert(supabaseItem)
            Result.success("Lost item added successfully")
        } catch (e: Exception) {
            Log.e("SupabaseRepository", "Failed to add lost item: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getLostItems(): Result<List<LostItem>> = withContext(Dispatchers.IO) {
        try {
            val response = supabase.from("lost_items")
                .select()
                .decodeList<SupabaseLostItem>()

            val lostItems = response.map { item ->
                LostItem(
                    id = item.id,
                    name = item.name,
                    description = item.description,
                    category = item.category,
                    location = item.lastSeenLocation,
                    imageUrl = item.imageUrl,
                    reportedBy = item.reportedBy,
                    reportedByName = item.reportedByName,
                    reportedDate = Timestamp(Date(item.reportedDate.toLongOrNull() ?: 0)),
                    dateLost = Timestamp(Date(item.dateLost.toLongOrNull() ?: 0)),
                    found = item.found,
                    foundBy = item.foundBy,
                    foundByName = item.foundByName
                )
            }

            Result.success(lostItems)
        } catch (e: Exception) {
            Log.e("SupabaseRepository", "Failed to get lost items: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun addFoundItem(foundItem: FoundItem): Result<String> = withContext(Dispatchers.IO) {
        try {
            val supabaseItem = SupabaseFoundItem(
                id = foundItem.id,
                name = foundItem.name,
                description = foundItem.description,
                category = foundItem.category,
                location = foundItem.location,
                keptAt = foundItem.keptAt,
                imageUrl = foundItem.imageUrl,
                reportedBy = foundItem.reportedBy,
                reportedByName = foundItem.reportedByName,
                reportedDate = foundItem.reportedDate.toDate().time.toString(),
                dateFound = foundItem.dateFound.toDate().time.toString(),
                claimed = foundItem.claimed,
                claimedBy = foundItem.claimedBy,
                claimedByName = foundItem.claimedByName
            )

            supabase.from("found_items").insert(supabaseItem)
            Result.success("Found item added successfully")
        } catch (e: Exception) {
            Log.e("SupabaseRepository", "Failed to add found item: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getFoundItems(): Result<List<FoundItem>> = withContext(Dispatchers.IO) {
        try {
            val response = supabase.from("found_items")
                .select()
                .decodeList<SupabaseFoundItem>()

            val foundItems = response.map { item ->
                FoundItem(
                    id = item.id,
                    name = item.name,
                    description = item.description,
                    category = item.category,
                    location = item.location,
                    keptAt = item.keptAt,
                    imageUrl = item.imageUrl,
                    reportedBy = item.reportedBy,
                    reportedByName = item.reportedByName,
                    reportedDate = Timestamp(Date(item.reportedDate.toLongOrNull() ?: 0)),
                    dateFound = Timestamp(Date(item.dateFound.toLongOrNull() ?: 0)),
                    claimed = item.claimed,
                    claimedBy = item.claimedBy,
                    claimedByName = item.claimedByName
                )
            }

            Result.success(foundItems)
        } catch (e: Exception) {
            Log.e("SupabaseRepository", "Failed to get found items: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getLostItemsByUser(userId: String): Result<List<LostItem>> = withContext(Dispatchers.IO) {
        try {
            // Get all items and filter in Kotlin for simplicity
            val response = supabase.from("lost_items")
                .select()
                .decodeList<SupabaseLostItem>()

            val filteredItems = response.filter { it.reportedBy == userId }

            val lostItems = filteredItems.map { item ->
                LostItem(
                    id = item.id,
                    name = item.name,
                    description = item.description,
                    category = item.category,
                    location = item.lastSeenLocation,
                    imageUrl = item.imageUrl,
                    reportedBy = item.reportedBy,
                    reportedByName = item.reportedByName,
                    reportedDate = Timestamp(Date(item.reportedDate.toLongOrNull() ?: 0)),
                    dateLost = Timestamp(Date(item.dateLost.toLongOrNull() ?: 0)),
                    found = item.found,
                    foundBy = item.foundBy,
                    foundByName = item.foundByName
                )
            }

            Result.success(lostItems)
        } catch (e: Exception) {
            Log.e("SupabaseRepository", "Failed to get user's lost items: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getFoundItemsByUser(userId: String): Result<List<FoundItem>> = withContext(Dispatchers.IO) {
        try {
            // Get all items and filter in Kotlin for simplicity
            val response = supabase.from("found_items")
                .select()
                .decodeList<SupabaseFoundItem>()

            val filteredItems = response.filter { it.reportedBy == userId }

            val foundItems = filteredItems.map { item ->
                FoundItem(
                    id = item.id,
                    name = item.name,
                    description = item.description,
                    category = item.category,
                    location = item.location,
                    keptAt = item.keptAt,
                    imageUrl = item.imageUrl,
                    reportedBy = item.reportedBy,
                    reportedByName = item.reportedByName,
                    reportedDate = Timestamp(Date(item.reportedDate.toLongOrNull() ?: 0)),
                    dateFound = Timestamp(Date(item.dateFound.toLongOrNull() ?: 0)),
                    claimed = item.claimed,
                    claimedBy = item.claimedBy,
                    claimedByName = item.claimedByName
                )
            }

            Result.success(foundItems)
        } catch (e: Exception) {
            Log.e("SupabaseRepository", "Failed to get user's found items: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun claimFoundItem(itemId: String, claimedBy: String, claimedByName: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Simple update operation
            supabase.from("found_items")
                .update(mapOf(
                    "claimed" to true,
                    "claimedBy" to claimedBy,
                    "claimedByName" to claimedByName
                ))

            Result.success("Item claimed successfully")
        } catch (e: Exception) {
            Log.e("SupabaseRepository", "Failed to claim item: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun markLostItemAsFound(itemId: String, foundBy: String, foundByName: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Simple update operation
            supabase.from("lost_items")
                .update(mapOf(
                    "found" to true,
                    "foundBy" to foundBy,
                    "foundByName" to foundByName
                ))

            Result.success("Item marked as found")
        } catch (e: Exception) {
            Log.e("SupabaseRepository", "Failed to mark item as found: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun deleteLostItem(itemId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Simple delete operation
            supabase.from("lost_items").delete()

            Result.success("Item deleted successfully")
        } catch (e: Exception) {
            Log.e("SupabaseRepository", "Failed to delete lost item: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun deleteFoundItem(itemId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Simple delete operation
            supabase.from("found_items").delete()

            Result.success("Item deleted successfully")
        } catch (e: Exception) {
            Log.e("SupabaseRepository", "Failed to delete found item: ${e.message}")
            Result.failure(e)
        }
    }
}
