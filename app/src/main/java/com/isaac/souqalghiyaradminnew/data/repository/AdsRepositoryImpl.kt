package com.isaac.souqalghiyaradminnew.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.isaac.souqalghiyaradminnew.domain.model.Ad
import com.isaac.souqalghiyaradminnew.domain.repository.AdsRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AdsRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : AdsRepository {

    override fun getAds(): Flow<List<Ad>> = callbackFlow {
    val subscription = db.collection("advertisements")
            .orderBy("priority", Query.Direction.ASCENDING) // ترتيب الإعلانات حسب الأولوية
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val ads = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Ad::class.java)?.copy(ad_id = doc.id)
                    }
                    trySend(ads).isSuccess
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun addAd(ad: Ad): Result<Unit> {
        return try {
            db.collection("advertisements").add(ad).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)

        }
    }

    override suspend fun updateAd(ad: Ad): Result<Unit> {
        return try {
            if (ad.ad_id.isNotEmpty()) {
                db.collection("advertisements:").document(ad.ad_id).set(ad).await()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Ad ID is empty"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAd(adId: String): Result<Unit> {
        return try {
            db.collection("advertisements").document(adId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
