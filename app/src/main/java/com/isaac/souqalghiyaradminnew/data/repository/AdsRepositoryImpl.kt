package com.isaac.souqalghiyaradminnew.data.repository

import com.google.firebase.firestore.FirebaseFirestore
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

    // ✅ تحديد اسم الجدول الفعلي في Firebase هنا:
    private val collectionRef = db.collection("advertisements")

    override fun getAds(): Flow<List<Ad>> = callbackFlow {
        val subscription = collectionRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                // تحويل الوثائق القادمة من Firebase إلى الموديل Ad
                val adsList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Ad::class.java)?.copy(ad_id = doc.id)
                }
                // إرسال البيانات للواجهة مرتبة حسب الأولوية ثم الأحدث
                trySend(adsList.sortedWith(compareByDescending<Ad> { it.priority }.thenByDescending { it.created_at.toString() })).isSuccess
            }
        }
        awaitClose { subscription.remove() }
    }

    override suspend fun addAd(ad: Ad): Result<Unit> {
        return try {
            val newDocRef = collectionRef.document()
            val newAd = ad.copy(ad_id = newDocRef.id)
            newDocRef.set(newAd).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateAd(ad: Ad): Result<Unit> {
        return try {
            collectionRef.document(ad.ad_id).set(ad).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAd(adId: String): Result<Unit> {
        return try {
            collectionRef.document(adId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}