package com.example.plantrecognizerui.history

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID

object HistoryManager {
    private val db = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()

    data class Entry(
        val id: String = UUID.randomUUID().toString(),
        val imageUri: String = "",
        val label: String = "",
        val confidence: Float = 0f,
        val timestamp: Long = 0L
    )

    suspend fun saveEntry(entry: Entry) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("history")
            .document(uid)
            .collection("entries")
            .document(entry.id)
            .set(entry)
            .await()
    }

    fun getHistory(): Flow<List<Entry>> = flow {
        val uid = auth.currentUser?.uid ?: return@flow emit(emptyList())
        val snapshot = db.collection("history")
            .document(uid)
            .collection("entries")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()
        val entries = snapshot.toObjects(Entry::class.java)
        emit(entries)
    }

    suspend fun deleteEntry(entry: Entry) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("history")
            .document(uid)
            .collection("entries")
            .document(entry.id)
            .delete()
            .await()
    }
}
