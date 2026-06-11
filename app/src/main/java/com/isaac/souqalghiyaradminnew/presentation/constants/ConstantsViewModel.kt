package com.isaac.souqalghiyaradminnew.presentation.constants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaac.souqalghiyaradminnew.domain.model.Brand
import com.isaac.souqalghiyaradminnew.domain.model.Location
import com.isaac.souqalghiyaradminnew.domain.model.QualityType
import com.isaac.souqalghiyaradminnew.domain.model.SparePartCategory
import com.isaac.souqalghiyaradminnew.domain.repository.ConstantsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConstantsViewModel @Inject constructor(
    private val repository: ConstantsRepository
) : ViewModel() {

    // جلب البيانات من الـ Repository
    val categories: StateFlow<List<SparePartCategory>> = repository.getSparePartCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val qualities: StateFlow<List<QualityType>> = repository.getQualityTypes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val brands: StateFlow<List<Brand>> = repository.getBrands()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val locations: StateFlow<List<Location>> = repository.getLocations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- عمليات الإضافة ---

    fun addCategory(categoryName: String) {
        viewModelScope.launch { repository.addSparePartCategory(categoryName) }
    }

    fun addQualityType(typeName: String) {
        viewModelScope.launch { repository.addQualityType(typeName) }
    }

    fun addBrand(brandName: String) {
        viewModelScope.launch { repository.addBrand(brandName) }
    }

    fun addLocation(locationName: String) {
        viewModelScope.launch { repository.addLocation(locationName) }
    }

    // --- عمليات التحديث ---

    fun updateCategory(id: String, newName: String) {
        viewModelScope.launch { repository.updateSparePartCategory(id, newName) }
    }

    fun updateQualityType(id: String, newName: String) {
        viewModelScope.launch { repository.updateQualityType(id, newName) }
    }

    fun updateBrand(id: String, newName: String) {
        viewModelScope.launch { repository.updateBrand(id, newName) }
    }

    fun updateLocation(id: String, newName: String) {
        viewModelScope.launch { repository.updateLocation(id, newName) }
    }

    // --- عمليات الحذف ---

    fun deleteCategory(id: String) {
        viewModelScope.launch { repository.deleteSparePartCategory(id) }
    }

    fun deleteQualityType(id: String) {
        viewModelScope.launch { repository.deleteQualityType(id) }
    }

    fun deleteBrand(id: String) {
        viewModelScope.launch { repository.deleteBrand(id) }
    }

    fun deleteLocation(id: String) {
        viewModelScope.launch { repository.deleteLocation(id) }
    }
}
