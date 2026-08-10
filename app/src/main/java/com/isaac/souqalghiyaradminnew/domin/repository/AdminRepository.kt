package com.isaac.souqalghiyaradminnew.domain.repository

import com.isaac.souqalghiyaradminnew.domain.model.UserEmp
import kotlinx.coroutines.flow.Flow

interface AdminRepository {
    // تم تغيير phoneNumber إلى email هنا
    suspend fun loginAdmin(email: String, password: String): UserEmp?
    
    fun observeAdminProfile(userId: String): Flow<UserEmp?>
    
    // دالة جديدة لتحديث التوكن الخاص بالموظف
    suspend fun updateFcmToken(userId: String, token: String): Result<Unit>
}
