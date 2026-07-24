package com.isaac.souqalghiyaradminnew.presentation.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.isaac.souqalghiyaradminnew.domain.model.OrderWithItems
import com.isaac.souqalghiyaradminnew.domain.repository.OrdersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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

    val pendingOrders: StateFlow<List<OrderWithItems>> = repository.getPendingOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val waitingOrders: StateFlow<List<OrderWithItems>> = repository.getWaitingOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadCanceledOrders: StateFlow<List<OrderWithItems>> = repository.getUnreadOrders("canceled")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadCompletedOrders: StateFlow<List<OrderWithItems>> = repository.getUnreadOrders("completed")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _canceledOrders = MutableStateFlow<List<OrderWithItems>>(emptyList())
    val canceledOrders: StateFlow<List<OrderWithItems>> = _canceledOrders.asStateFlow()

    private val _completedOrders = MutableStateFlow<List<OrderWithItems>>(emptyList())
    val completedOrders: StateFlow<List<OrderWithItems>> = _completedOrders.asStateFlow()

    private val _latestCompletedOrders = MutableStateFlow<List<OrderWithItems>>(emptyList())
    val latestCompletedOrders: StateFlow<List<OrderWithItems>> = _latestCompletedOrders.asStateFlow()

    private val _latestCanceledOrders = MutableStateFlow<List<OrderWithItems>>(emptyList())
    val latestCanceledOrders: StateFlow<List<OrderWithItems>> = _latestCanceledOrders.asStateFlow()

    private val _isLoadingHistorical = MutableStateFlow(false)
    val isLoadingHistorical: StateFlow<Boolean> = _isLoadingHistorical.asStateFlow()

    // --- الإضافة الخاصة بجلب رقم هاتف العميل من جدول users ---
    private val _userPhones = MutableStateFlow<Map<String, String>>(emptyMap())
    val userPhones: StateFlow<Map<String, String>> = _userPhones.asStateFlow()

    fun fetchUserPhone(userId: String) {
        if (userId.isEmpty() || _userPhones.value.containsKey(userId)) return
        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                // أولاً: نبحث بداخل جدول users حيث user_id يطابق المعرف
                val snapshot = db.collection("users").whereEqualTo("user_id", userId).get().await()
                if (!snapshot.isEmpty) {
                    val phone = snapshot.documents.first().getString("phone_number") ?: "غير متوفر"
                    _userPhones.value = _userPhones.value.toMutableMap().apply { put(userId, phone) }
                } else {
                    // في حال كان الـ Document ID هو نفسه الـ user_id
                    val doc = db.collection("users").document(userId).get().await()
                    val phone = doc.getString("phone_number") ?: "غير متوفر"
                    _userPhones.value = _userPhones.value.toMutableMap().apply { put(userId, phone) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _userPhones.value = _userPhones.value.toMutableMap().apply { put(userId, "غير متوفر") }
            }
        }
    }
    // -----------------------------------------------------------

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

    fun rejectOrder(orderId: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, "canceled", 0.0)
        }
    }

    fun markOrderAsReadAndRemoveAlarm(orderNumber: Long) {
        viewModelScope.launch {
            repository.deleteAdminAlarmByOrderNumber(orderNumber)
        }
    }

    fun fetchOrdersByDate(status: String, startTimestamp: Long, endTimestamp: Long) {
        viewModelScope.launch {
            _isLoadingHistorical.value = true
            val result = repository.getOrdersByDateRange(status, startTimestamp, endTimestamp)

            if (status == "canceled") {
                _canceledOrders.value = result
            } else if (status == "completed") {
                _completedOrders.value = result
            }
            _isLoadingHistorical.value = false
        }
    }

    fun fetchLatestOrders(status: String) {
        viewModelScope.launch {
            try {
                val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
                val result = repository.getOrdersByDateRange(status, thirtyDaysAgo, System.currentTimeMillis())

                val top3 = result.take(3)

                if (status == "completed") {
                    _latestCompletedOrders.value = top3
                } else {
                    _latestCanceledOrders.value = top3
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}