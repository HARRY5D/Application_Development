package com.example.campus_lost_found.repository

import android.util.Log
import com.example.campus_lost_found.model.FoundItem
import com.example.campus_lost_found.model.LostItem
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

class ItemRepository {
    private val db = FirebaseFirestore.getInstance()
    private val lostItemsCollection = db.collection("lostItems")
    private val foundItemsCollection = db.collection("foundItems")

    // Lost Items
    fun addLostItem(lostItem: LostItem): Task<Void> {
        val documentRef = lostItemsCollection.document()
        lostItem.id = documentRef.id
        return documentRef.set(lostItem)
    }

    fun getLostItems(): Query {
        return lostItemsCollection.orderBy("reportedDate", Query.Direction.DESCENDING)
    }

    fun getLostItemsByUser(userId: String): Query {
        return lostItemsCollection.whereEqualTo("reportedBy", userId)
            .orderBy("reportedDate", Query.Direction.DESCENDING)
    }

    fun getLostItem(itemId: String): Task<DocumentSnapshot> {
        return lostItemsCollection.document(itemId).get()
    }

    fun updateLostItem(lostItem: LostItem): Task<Void> {
        return lostItemsCollection.document(lostItem.id).set(lostItem)
    }

    fun deleteLostItem(itemId: String, userId: String): Task<Void> {
        return lostItemsCollection.document(itemId).delete()
    }

    // Found Items
    fun addFoundItem(foundItem: FoundItem): Task<Void> {
        val documentRef = foundItemsCollection.document()
        foundItem.id = documentRef.id
        return documentRef.set(foundItem)
    }

    fun getFoundItems(): Query {
        return foundItemsCollection.orderBy("reportedDate", Query.Direction.DESCENDING)
    }

    fun getFoundItemsByUser(userId: String): Query {
        return foundItemsCollection.whereEqualTo("reportedBy", userId)
            .orderBy("reportedDate", Query.Direction.DESCENDING)
    }

    fun getFoundItem(itemId: String): Task<DocumentSnapshot> {
        return foundItemsCollection.document(itemId).get()
    }

    fun updateFoundItem(foundItem: FoundItem): Task<Void> {
        return foundItemsCollection.document(foundItem.id).set(foundItem)
    }

    fun deleteFoundItem(itemId: String, userId: String): Task<Void> {
        return foundItemsCollection.document(itemId).delete()
    }

    fun claimItem(itemId: String, claimedBy: String, claimedByName: String): Task<Void> {
        return foundItemsCollection.document(itemId).update(
            mapOf(
                "claimed" to true,
                "claimedBy" to claimedBy,
                "claimedByName" to claimedByName
            )
        )
    }

    fun markItemAsFound(itemId: String, foundBy: String): Task<Void> {
        return lostItemsCollection.document(itemId).update(
            mapOf(
                "found" to true,
                "foundBy" to foundBy
            )
        )
    }
}
