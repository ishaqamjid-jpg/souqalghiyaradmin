package com.isaac.souqalghiyaradminnew.presentation.ads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.isaac.souqalghiyaradminnew.domain.model.Ad
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

    // جلب جميع الإعلانات من قاعدة البيانات
    private val allAds = repository.getAds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // فحص الإعلانات المنتهية يحدث فقط عند قدوم بيانات جديدة من السيرفر
        viewModelScope.launch {
            allAds.collect { ads ->
                checkAndDeactivateExpiredAds(ads)
            }
        }
    }

    // دمج الإعلانات مع نص البحث
    val filteredAds = combine(allAds, _searchQuery) { ads, query ->
        if (query.isBlank()) {
            ads
        } else {
            ads.filter { it.name.contains(query, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // دالة ذكية لإيقاف الإعلانات التي انتهى تاريخها
    private fun checkAndDeactivateExpiredAds(ads: List<Ad>) {
        val currentTimeSeconds = Timestamp.now().seconds
        ads.forEach { ad ->
            // استخدام let الآمن لحل مشكلة الـ Smart Cast
            if (ad.is_active) {
                ad.end_date?.let { endDate ->
                    // إذا كان تاريخ الانتهاء أصغر من الوقت الحالي (أي مضى وانتهى)
                    if (endDate.seconds < currentTimeSeconds) {
                        viewModelScope.launch {
                            // تحديث الإعلان ليصبح غير نشط في قاعدة البيانات
                            repository.updateAd(ad.copy(is_active = false))
                        }
                    }
                }
            }
        }
    }

    // دالة لإضافة أو تعديل إعلان
    fun saveAd(ad: Ad) {
        viewModelScope.launch {
            if (ad.ad_id.isEmpty()) {
                // إضافة إعلان جديد مع تعيين وقت الإنشاء
                repository.addAd(ad.copy(created_at = Timestamp.now()))
            } else {
                // تحديث إعلان موجود
                repository.updateAd(ad)
            }
        }
    }

    // دالة لتفعيل أو إيقاف الإعلان يدوياً
    fun toggleAdStatus(ad: Ad) {
        viewModelScope.launch {
            repository.updateAd(ad.copy(is_active = !ad.is_active))
        }
    }

    // دالة لحذف الإعلان
    fun deleteAd(adId: String) {
        viewModelScope.launch {
            repository.deleteAd(adId)
        }
    }
}