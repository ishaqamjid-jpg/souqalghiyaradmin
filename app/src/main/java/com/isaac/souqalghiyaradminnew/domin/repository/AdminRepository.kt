package com.isaac.souqalghiyaradminnew.domain.repository

import com.isaac.souqalghiyaradminnew.domain.model.UserEmp
import kotlinx.coroutines.flow.Flow // تأكد من إضافة هذا الاستيراد

interface AdminRepository {
    // دالة تسجيل الدخول
    suspend fun loginAdmin(phoneNumber: String): UserEmp?

    // الدالة الجديدة لمراقبة حساب الموظف لحظياً (هذا هو السطر الناقص)
    fun observeAdminProfile(userId: String): Flow<UserEmp?>
}