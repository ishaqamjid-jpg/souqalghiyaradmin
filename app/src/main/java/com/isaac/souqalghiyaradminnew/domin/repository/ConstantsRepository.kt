package com.isaac.souqalghiyaradminnew.domain.repository

import com.isaac.souqalghiyaradminnew.domain.model.Brand
import com.isaac.souqalghiyaradminnew.domain.model.Location
import com.isaac.souqalghiyaradminnew.domain.model.QualityType
import com.isaac.souqalghiyaradminnew.domain.model.SparePartCategory
import kotlinx.coroutines.flow.Flow

interface ConstantsRepository {
    // أسماء القطع
    fun getSparePartCategories(): Flow<List<SparePartCategory>>
    suspend fun addSparePartCategory(name: String): Result<Unit>
    suspend fun updateSparePartCategory(id: String, newName: String): Result<Unit>
    suspend fun deleteSparePartCategory(id: String): Result<Unit>

    // أنواع الجودة
    fun getQualityTypes(): Flow<List<QualityType>>
    suspend fun addQualityType(name: String): Result<Unit>
    suspend fun updateQualityType(id: String, newName: String): Result<Unit>
    suspend fun deleteQualityType(id: String): Result<Unit>

    // الماركات (جديد)
    fun getBrands(): Flow<List<Brand>>
    suspend fun addBrand(name: String): Result<Unit>
    suspend fun updateBrand(id: String, newName: String): Result<Unit>
    suspend fun deleteBrand(id: String): Result<Unit>

    // المحافظات (جديد)
    fun getLocations(): Flow<List<Location>>
    suspend fun addLocation(name: String): Result<Unit>
    suspend fun updateLocation(id: String, newName: String): Result<Unit>
    suspend fun deleteLocation(id: String): Result<Unit>
}
