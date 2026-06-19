package com.isaac.souqalghiyaradminnew.presentation.ads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.isaac.souqalghiyaradminnew.domain.model.Ad
import com.isaac.souqalghiyaradminnew.domain.repository.AdsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdsViewModel @Inject constructor(
    private val repository: AdsRepository
) : ViewModel() {

    // جلب الإعلانات وتحديثها لحظياً
    val ads = repository.getAds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // دالة لإضافة أو تعديل إعلان (بناءً على وجود ad_id)
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

    // دالة لتفعيل أو إيقاف الإعلان السريع (تغيير الـ is_active)
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