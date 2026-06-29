package com.isaac.souqalghiyaradminnew.presentation.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaac.souqalghiyaradminnew.domain.model.OrderWithItems
import com.isaac.souqalghiyaradminnew.domain.repository.OrdersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ItemAdminPricing(
    val itemId: String,
    val purchasePrice: Double,
    val sellingPrice: Double,
    val providerName: String,
    val invoiceNumber: String
)

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val repository: OrdersRepository
) : ViewModel() {

    // 1. الطلبات المعلقة
    val pendingOrders: StateFlow<List<OrderWithItems>> = repository.getPendingOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 2. الطلبات قيد الموافقة
    val waitingOrders: StateFlow<List<OrderWithItems>> = repository.getWaitingOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 3. الطلبات المرفوضة (مستقلة)
    private val _canceledOrders = MutableStateFlow<List<OrderWithItems>>(emptyList())
    val canceledOrders: StateFlow<List<OrderWithItems>> = _canceledOrders.asStateFlow()

    // 4. الطلبات المكتملة (مستقلة)
    private val _completedOrders = MutableStateFlow<List<OrderWithItems>>(emptyList())
    val completedOrders: StateFlow<List<OrderWithItems>> = _completedOrders.asStateFlow()

    private val _isLoadingHistorical = MutableStateFlow(false)
    val isLoadingHistorical: StateFlow<Boolean> = _isLoadingHistorical.asStateFlow()

    // دالة الموافقة وتعبئة الأسعار
    fun approveOrder(
        orderId: String,
        deliveryFees: Double,
        itemsPricing: List<ItemAdminPricing>
    ) {
        viewModelScope.launch {
            itemsPricing.forEach { pricing ->
                repository.updateOrderItemAdminFields(
                    orderId = orderId,
                    itemId = pricing.itemId,
                    purchasePrice = pricing.purchasePrice,
                    sellingPrice = pricing.sellingPrice,
                    providerName = pricing.providerName,
                    invoiceNumber = pricing.invoiceNumber
                )
            }
            repository.updateOrderStatus(orderId, "waiting for approvel", deliveryFees)
        }
    }

    // دالة رفض الطلب
    fun rejectOrder(orderId: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, "canceled", 0.0)
        }
    }

    // دالة جلب الطلبات حسب التاريخ والحالة وتوجيهها للمتغير الصحيح
    fun fetchOrdersByDate(status: String, startTimestamp: Long, endTimestamp: Long) {
        viewModelScope.launch {
            _isLoadingHistorical.value = true
            val result = repository.getOrdersByDateRange(status, startTimestamp, endTimestamp)

            // توجيه النتائج للقسم الصحيح حتى لا تتداخل
            if (status == "canceled") {
                _canceledOrders.value = result
            } else if (status == "completed") {
                _completedOrders.value = result
            }

            _isLoadingHistorical.value = false
        }
    }
}
