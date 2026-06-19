package com.isaac.souqalghiyaradminnew.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaac.souqalghiyaradminnew.domain.repository.AdminRepository
import com.isaac.souqalghiyaradminnew.domain.repository.OrdersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    private val ordersRepository: OrdersRepository // تم حقن مستودع الطلبات هنا
) : ViewModel() {

    // جلب عدد الطلبات المعلقة الحقيقي بشكل لحظي من الفايربيز
    val pendingOrdersCount: StateFlow<Int> = ordersRepository.getPendingOrders()
        .map { orders -> orders.size } // نأخذ حجم القائمة (عدد الطلبات)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

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