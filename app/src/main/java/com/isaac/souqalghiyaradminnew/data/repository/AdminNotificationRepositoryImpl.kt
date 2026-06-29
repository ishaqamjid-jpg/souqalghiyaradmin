package com.isaac.souqalghiyaradminnew.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.isaac.souqalghiyaradminnew.domain.model.admin_alarm
import com.isaac.souqalghiyaradminnew.domain.repository.AdminNotificationRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AdminNotificationRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : AdminNotificationRepository {

    override fun getAdminNotifications(): Flow<List<admin_alarm>> = callbackFlow {
        val subscription = db.collection("admin_alarm")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val alarms = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(admin_alarm::class.java)?.copy(alarm_id = doc.id)
                    }
                    // ترتيب الإشعارات من الأحدث للأقدم
                    trySend(alarms.sortedByDescending { it.date }).isSuccess
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun markNotificationAsRead(alarmId: String): Result<Unit> {
        return try {
            db.collection("admin_alarm").document(alarmId).update("isRead", true).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteNotification(alarmId: String): Result<Unit> {
        return try {
            db.collection("admin_alarm").document(alarmId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
