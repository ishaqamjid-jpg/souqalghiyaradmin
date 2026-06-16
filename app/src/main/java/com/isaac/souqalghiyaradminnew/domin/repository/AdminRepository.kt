package com.isaac.souqalghiyaradminnew.domain.repository

import com.isaac.souqalghiyaradminnew.domain.model.UserEmp
import kotlinx.coroutines.flow.Flow 

interface AdminRepository {
    // دالة تسجيل الدخول (تم إضافة كلمة المرور)
    suspend fun loginAdmin(phoneNumber: String, password: String): UserEmp?

    // دالة مراقبة حساب الموظف لحظياً
    fun observeAdminProfile(userId: String): Flow<UserEmp?>
}
