package com.isaac.souqalghiyaradminnew.domain.repository

import com.isaac.souqalghiyaradminnew.domain.model.Ad
import kotlinx.coroutines.flow.Flow

interface AdsRepository {
    fun getAds(): Flow<List<Ad>>
    suspend fun addAd(ad: Ad): Result<Unit>
    suspend fun updateAd(ad: Ad): Result<Unit>
    suspend fun deleteAd(adId: String): Result<Unit>
}
