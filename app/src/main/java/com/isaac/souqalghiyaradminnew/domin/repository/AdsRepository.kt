package com.isaac.souqalghiyaradminnew.domain.repository

import com.isaac.souqalghiyaradminnew.domain.model.Ad
import com.isaac.souqalghiyaradminnew.domain.model.PublicAdvertisement
import kotlinx.coroutines.flow.Flow

interface AdsRepository {
    // الإعلانات التجارية
    fun getAds(): Flow<List<Ad>>
    suspend fun addAd(ad: Ad): Result<Unit>
    suspend fun updateAd(ad: Ad): Result<Unit>
    suspend fun deleteAd(adId: String): Result<Unit>

    // الإعلانات العامة (الإشعارات)
    fun getPublicAds(): Flow<List<PublicAdvertisement>>
    suspend fun addPublicAd(ad: PublicAdvertisement): Result<Unit>
    suspend fun deletePublicAd(docId: String): Result<Unit>
}
