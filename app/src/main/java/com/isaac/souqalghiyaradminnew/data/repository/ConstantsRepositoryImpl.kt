package com.isaac.souqalghiyaradminnew.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.isaac.souqalghiyaradminnew.domain.model.*
import com.isaac.souqalghiyaradminnew.domain.repository.ConstantsRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ConstantsRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : ConstantsRepository {

    // --- Spare Parts Categories ---
    override fun getSparePartCategories(): Flow<List<SparePartCategory>> = callbackFlow {
        val sub = db.collection("spare_parts_categories").addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(SparePartCategory::class.java)?.copy(id = doc.id)
                }
                trySend(list).isSuccess
            }
        }
        awaitClose { sub.remove() }
    }

    override suspend fun addSparePartCategory(name: String): Result<Unit> = try {
        db.collection("spare_parts_categories").add(mapOf("spare_parts_categories" to name)).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun updateSparePartCategory(id: String, newName: String): Result<Unit> = try {
        db.collection("spare_parts_categories").document(id).update("spare_parts_categories", newName).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun deleteSparePartCategory(id: String): Result<Unit> = try {
        db.collection("spare_parts_categories").document(id).delete().await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    // --- Quality Types ---
    override fun getQualityTypes(): Flow<List<QualityType>> = callbackFlow {
        val sub = db.collection("quality_types").addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(QualityType::class.java)?.copy(id = doc.id)
                }
                trySend(list).isSuccess
            }
        }
        awaitClose { sub.remove() }
    }

    override suspend fun addQualityType(name: String): Result<Unit> = try {
        db.collection("quality_types").add(mapOf("quality_types" to name)).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun updateQualityType(id: String, newName: String): Result<Unit> = try {
        db.collection("quality_types").document(id).update("quality_types", newName).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun deleteQualityType(id: String): Result<Unit> = try {
        db.collection("quality_types").document(id).delete().await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    // --- Brands ---
    override fun getBrands(): Flow<List<Brand>> = callbackFlow {
        val sub = db.collection("brands").addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Brand::class.java)?.copy(id = doc.id)
                }
                trySend(list).isSuccess
            }
        }
        awaitClose { sub.remove() }
    }

    override suspend fun addBrand(name: String): Result<Unit> = try {
        db.collection("brands").add(mapOf("brand_name" to name)).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun updateBrand(id: String, newName: String): Result<Unit> = try {
        db.collection("brands").document(id).update("brand_name", newName).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun deleteBrand(id: String): Result<Unit> = try {
        db.collection("brands").document(id).delete().await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    // --- Locations ---
    override fun getLocations(): Flow<List<Location>> = callbackFlow {
        val sub = db.collection("location").addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Location::class.java)?.copy(id = doc.id)
                }
                trySend(list).isSuccess
            }
        }
        awaitClose { sub.remove() }
    }

    override suspend fun addLocation(name: String): Result<Unit> = try {
        db.collection("location").add(mapOf("location" to name)).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun updateLocation(id: String, newName: String): Result<Unit> = try {
        db.collection("location").document(id).update("location", newName).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun deleteLocation(id: String): Result<Unit> = try {
        db.collection("location").document(id).delete().await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }
}
