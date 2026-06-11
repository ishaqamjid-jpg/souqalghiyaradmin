package com.isaac.souqalghiyaradminnew.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaac.souqalghiyaradminnew.domain.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    // حالة مؤقتة لعدد الطلبات المعلقة (يمكنك ربطها لاحقاً بـ OrdersRepository)
    private val _pendingOrdersCount = MutableStateFlow(3)
    val pendingOrdersCount: StateFlow<Int> = _pendingOrdersCount.asStateFlow()

    // حالات الأمان والمراقبة
    private val _isAccountBanned = MutableStateFlow(false)
    val isAccountBanned = _isAccountBanned.asStateFlow()

    private val _userPermissions = MutableStateFlow("employee")
    val userPermissions = _userPermissions.asStateFlow()

    // دالة لمراقبة حساب الموظف بمجرد دخوله
    fun startMonitoringAccount(currentUserId: String) {
        viewModelScope.launch {
            adminRepository.observeAdminProfile(currentUserId).collect { user ->
                if (user == null || user.status != "active") {
                    _isAccountBanned.value = true // طرد فوري
                } else {
                    _userPermissions.value = user.user_permissions // تحديث الصلاحية
                }
            }
        }
    }

    // دالة لتسجيل الخروج الإرادي
    fun logout(onLogoutSuccess: () -> Unit) {
        // سيتم استدعاء مسح الـ SharedPreferences من الـ Screen
        onLogoutSuccess()
    }
}
