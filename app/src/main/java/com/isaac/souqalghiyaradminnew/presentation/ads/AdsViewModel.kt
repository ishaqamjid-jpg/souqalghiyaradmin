package com.isaac.souqalghiyaradminnew.presentation.ads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.isaac.souqalghiyaradminnew.domain.model.Ad
import com.isaac.souqalghiyaradminnew.domain.model.PublicAdvertisement
import com.isaac.souqalghiyaradminnew.domain.repository.AdsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdsViewModel @Inject constructor(
    private val repository: AdsRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // جلب جميع الإعلانات التجارية
    private val allAds = repository.getAds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // جلب جميع الإعلانات العامة
    private val allPublicAds = repository.getPublicAds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            allAds.collect { ads ->
                checkAndDeactivateExpiredAds(ads)
            }
        }
    }

    // دمج الإعلانات التجارية مع نص البحث
    val filteredAds = combine(allAds, _searchQuery) { ads, query ->
        if (query.isBlank()) ads else ads.filter { it.name.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // دمج الإعلانات العامة مع نص البحث
    val filteredPublicAds = combine(allPublicAds, _searchQuery) { ads, query ->
        if (query.isBlank()) ads else ads.filter { it.title.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private fun checkAndDeactivateExpiredAds(ads: List<Ad>) {
        val currentTimeSeconds = Timestamp.now().seconds
        ads.forEach { ad ->
            if (ad.is_active) {
                ad.end_date?.let { endDate ->
                    if (endDate.seconds < currentTimeSeconds) {
                        viewModelScope.launch {
                            repository.updateAd(ad.copy(is_active = false))
                        }
                    }
                }
            }
        }
    }

    fun saveAd(ad: Ad) {
        viewModelScope.launch {
            if (ad.ad_id.isEmpty()) {
                repository.addAd(ad.copy(created_at = Timestamp.now()))
            } else {
                repository.updateAd(ad)
            }
        }
    }

    fun toggleAdStatus(ad: Ad) {
        viewModelScope.launch { repository.updateAd(ad.copy(is_active = !ad.is_active)) }
    }

    fun deleteAd(adId: String) {
        viewModelScope.launch { repository.deleteAd(adId) }
    }

    // --- دوال الإعلانات العامة ---
    fun savePublicAd(ad: PublicAdvertisement) {
        viewModelScope.launch { repository.addPublicAd(ad) }
    }

    fun deletePublicAd(docId: String) {
        viewModelScope.launch { repository.deletePublicAd(docId) }
    }
}
