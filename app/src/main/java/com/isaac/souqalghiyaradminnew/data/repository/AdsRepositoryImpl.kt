package com.isaac.souqalghiyaradminnew.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.isaac.souqalghiyaradminnew.domain.model.Ad
import com.isaac.souqalghiyaradminnew.domain.model.PublicAdvertisement
import com.isaac.souqalghiyaradminnew.domain.repository.AdsRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AdsRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : AdsRepository {

    private val collectionRef = db.collection("advertisements")
    private val publicAdsCollectionRef = db.collection("public_advertisements")

    // --- الإعلانات التجارية ---
    override fun getAds(): Flow<List<Ad>> = callbackFlow {
        val subscription = collectionRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val adsList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Ad::class.java)?.copy(ad_id = doc.id)
                }
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

    // --- الإعلانات العامة (الإشعارات) ---
    override fun getPublicAds(): Flow<List<PublicAdvertisement>> = callbackFlow {
        val subscription = publicAdsCollectionRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val publicAdsList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(PublicAdvertisement::class.java)?.copy(doc_id = doc.id)
                }
                // ترتيب الإعلانات العامة من الأحدث للأقدم
                trySend(publicAdsList.sortedByDescending { it.create_date?.seconds }).isSuccess
            }
        }
        awaitClose { subscription.remove() }
    }

    override suspend fun addPublicAd(ad: PublicAdvertisement): Result<Unit> {
        return try {
            val newDocRef = publicAdsCollectionRef.document()
            val newAd = ad.copy(doc_id = newDocRef.id, create_date = com.google.firebase.Timestamp.now())
            newDocRef.set(newAd).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePublicAd(docId: String): Result<Unit> {
        return try {
            publicAdsCollectionRef.document(docId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
