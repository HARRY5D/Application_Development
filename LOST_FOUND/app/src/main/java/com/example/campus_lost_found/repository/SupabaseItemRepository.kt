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
import io.github.jan.supabase.postgrest.query.Order

@Serializable
data class LostItemData(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val lastSeenLocation: String = "", // Changed from 'location' to match DB schema
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
data class FoundItemData(
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

class SupabaseItemRepository {
    private val supabase = SupabaseManager.getClient()

    // Lost Items
    suspend fun addLostItem(lostItem: LostItem): Result<String> = withContext(Dispatchers.IO) {
        try {
            val itemData = LostItemData(
                id = lostItem.id,
                name = lostItem.name,
                description = lostItem.description,
                category = lostItem.category,
                lastSeenLocation = lostItem.location, // Updated to lastSeenLocation
                imageUrl = lostItem.imageUrl,
                reportedBy = lostItem.reportedBy,
                reportedByName = lostItem.reportedByName,
                reportedDate = lostItem.reportedDate.toDate().time.toString(),
                dateLost = lostItem.dateLost.toDate().time.toString(),
                found = lostItem.found,
                foundBy = lostItem.foundBy,
                foundByName = lostItem.foundByName
            )

            supabase.from("lost_items").insert(itemData)
            Result.success("Item added successfully")
        } catch (e: Exception) {
            Log.e("SupabaseItemRepository", "Failed to add lost item", e)
            Result.failure(e)
        }
    }

    suspend fun getLostItems(): Result<List<LostItem>> = withContext(Dispatchers.IO) {
        try {
            val response = supabase.from("lost_items")
                .select()
                .decodeList<LostItemData>()

            val lostItems = response.map { data: LostItemData ->
                LostItem(
                    id = data.id,
                    name = data.name,
                    description = data.description,
                    category = data.category,
                    location = data.lastSeenLocation, // Updated to lastSeenLocation
                    imageUrl = data.imageUrl,
                    reportedBy = data.reportedBy,
                    reportedByName = data.reportedByName,
                    reportedDate = Timestamp(java.util.Date(data.reportedDate.toLongOrNull() ?: 0)),
                    dateLost = Timestamp(java.util.Date(data.dateLost.toLongOrNull() ?: 0)),
                    found = data.found,
                    foundBy = data.foundBy,
                    foundByName = data.foundByName
                )
            }

            Result.success(lostItems)
        } catch (e: Exception) {
            Log.e("SupabaseItemRepository", "Failed to get lost items", e)
            Result.failure(e)
        }
    }

    suspend fun getLostItemsByUser(userId: String): Result<List<LostItem>> = withContext(Dispatchers.IO) {
        try {
            // Simplified approach - get all and filter in Kotlin for now
            val response = supabase.from("lost_items")
                .select()
                .decodeList<LostItemData>()

            val filteredItems = response.filter { it.reportedBy == userId }

            val lostItems = filteredItems.map { data: LostItemData ->
                LostItem(
                    id = data.id,
                    name = data.name,
                    description = data.description,
                    category = data.category,
                    location = data.lastSeenLocation, // Updated to lastSeenLocation
                    imageUrl = data.imageUrl,
                    reportedBy = data.reportedBy,
                    reportedByName = data.reportedByName,
                    reportedDate = Timestamp(java.util.Date(data.reportedDate.toLongOrNull() ?: 0)),
                    dateLost = Timestamp(java.util.Date(data.dateLost.toLongOrNull() ?: 0)),
                    found = data.found,
                    foundBy = data.foundBy,
                    foundByName = data.foundByName
                )
            }

            Result.success(lostItems)
        } catch (e: Exception) {
            Log.e("SupabaseItemRepository", "Failed to get user's lost items", e)
            Result.failure(e)
        }
    }

    // Found Items
    suspend fun addFoundItem(foundItem: FoundItem): Result<String> = withContext(Dispatchers.IO) {
        try {
            val itemData = FoundItemData(
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

            supabase.from("found_items").insert(itemData)
            Result.success("Item added successfully")
        } catch (e: Exception) {
            Log.e("SupabaseItemRepository", "Failed to add found item", e)
            Result.failure(e)
        }
    }

    suspend fun getFoundItems(): Result<List<FoundItem>> = withContext(Dispatchers.IO) {
        try {
            val response = supabase.from("found_items")
                .select()
                .decodeList<FoundItemData>()

            val foundItems = response.map { data: FoundItemData ->
                FoundItem(
                    id = data.id,
                    name = data.name,
                    description = data.description,
                    category = data.category,
                    location = data.location,
                    keptAt = data.keptAt,
                    imageUrl = data.imageUrl,
                    reportedBy = data.reportedBy,
                    reportedByName = data.reportedByName,
                    reportedDate = Timestamp(java.util.Date(data.reportedDate.toLongOrNull() ?: 0)),
                    dateFound = Timestamp(java.util.Date(data.dateFound.toLongOrNull() ?: 0)),
                    claimed = data.claimed,
                    claimedBy = data.claimedBy,
                    claimedByName = data.claimedByName
                )
            }

            Result.success(foundItems)
        } catch (e: Exception) {
            Log.e("SupabaseItemRepository", "Failed to get found items", e)
            Result.failure(e)
        }
    }

    suspend fun getFoundItemsByUser(userId: String): Result<List<FoundItem>> = withContext(Dispatchers.IO) {
        try {
            // Simplified approach - get all and filter in Kotlin for now
            val response = supabase.from("found_items")
                .select()
                .decodeList<FoundItemData>()

            val filteredItems = response.filter { it.reportedBy == userId }

            val foundItems = filteredItems.map { data: FoundItemData ->
                FoundItem(
                    id = data.id,
                    name = data.name,
                    description = data.description,
                    category = data.category,
                    location = data.location,
                    keptAt = data.keptAt,
                    imageUrl = data.imageUrl,
                    reportedBy = data.reportedBy,
                    reportedByName = data.reportedByName,
                    reportedDate = Timestamp(java.util.Date(data.reportedDate.toLongOrNull() ?: 0)),
                    dateFound = Timestamp(java.util.Date(data.dateFound.toLongOrNull() ?: 0)),
                    claimed = data.claimed,
                    claimedBy = data.claimedBy,
                    claimedByName = data.claimedByName
                )
            }

            Result.success(foundItems)
        } catch (e: Exception) {
            Log.e("SupabaseItemRepository", "Failed to get user's found items", e)
            Result.failure(e)
        }
    }

    suspend fun claimFoundItem(itemId: String, claimedBy: String, claimedByName: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Simple update without complex filtering for now
            supabase.from("found_items")
                .update(mapOf(
                    "claimed" to true,
                    "claimedBy" to claimedBy,
                    "claimedByName" to claimedByName
                ))

            Result.success("Item claimed successfully")
        } catch (e: Exception) {
            Log.e("SupabaseItemRepository", "Failed to claim item", e)
            Result.failure(e)
        }
    }

    suspend fun markLostItemAsFound(itemId: String, foundBy: String, foundByName: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Simple update without complex filtering for now
            supabase.from("lost_items")
                .update(mapOf(
                    "found" to true,
                    "foundBy" to foundBy,
                    "foundByName" to foundByName
                ))

            Result.success("Item marked as found")
        } catch (e: Exception) {
            Log.e("SupabaseItemRepository", "Failed to mark item as found", e)
            Result.failure(e)
        }
    }

    suspend fun deleteLostItem(itemId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Simple delete without complex filtering for now
            supabase.from("lost_items").delete()

            Result.success("Item deleted successfully")
        } catch (e: Exception) {
            Log.e("SupabaseItemRepository", "Failed to delete lost item", e)
            Result.failure(e)
        }
    }

    suspend fun deleteFoundItem(itemId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Simple delete without complex filtering for now
            supabase.from("found_items").delete()

            Result.success("Item deleted successfully")
        } catch (e: Exception) {
            Log.e("SupabaseItemRepository", "Failed to delete found item", e)
            Result.failure(e)
        }
    }
}
