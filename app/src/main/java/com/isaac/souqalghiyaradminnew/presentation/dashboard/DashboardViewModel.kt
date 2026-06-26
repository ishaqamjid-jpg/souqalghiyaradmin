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
    private val ordersRepository: OrdersRepository 
) : ViewModel() {

    val pendingOrdersCount: StateFlow<Int> = ordersRepository.getPendingOrders()
        .map { orders -> orders.size } 
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _isAccountBanned = MutableStateFlow(false)
    val isAccountBanned = _isAccountBanned.asStateFlow()

    private val _userPermissions = MutableStateFlow("employee")
    val userPermissions = _userPermissions.asStateFlow()

    fun startMonitoringAccount(currentUserId: String) {
        viewModelScope.launch {
            adminRepository.observeAdminProfile(currentUserId).collect { user ->
                if (user == null || user.status != "active") {
                    _isAccountBanned.value = true 
                } else {
                    _userPermissions.value = user.user_permissions 
                }
            }
        }
    }

    fun logout(onLogoutSuccess: () -> Unit) {
        onLogoutSuccess()
    }
}
