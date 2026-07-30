package com.isaac.souqalghiyaradminnew.presentation.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaac.souqalghiyaradminnew.domain.model.OrderWithItems
import com.isaac.souqalghiyaradminnew.domain.repository.OrdersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

data class ReportStats(
    val totalCompletedOrders: Int = 0,
    val totalCanceledOrders: Int = 0,
    val totalTransportation: Double = 0.0,
    val totalCosts: Double = 0.0,
    val totalRevenue: Double = 0.0,
    val netProfit: Double = 0.0
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val repository: OrdersRepository
) : ViewModel() {

    // متغيرات البحث
    val merchantName = MutableStateFlow("")
    val partName = MutableStateFlow("")
    val orderNumber = MutableStateFlow("")
    val vehicleModel = MutableStateFlow("")
    val orderStatus = MutableStateFlow("")
    val fromDate = MutableStateFlow("")
    val toDate = MutableStateFlow("")
    val isDateFilterEnabled = MutableStateFlow(false)

    val hasSearched = MutableStateFlow(false)

    // جلب جميع الطلبات
    private val allOrders = repository.getAllOrdersForReports()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _filteredOrders = MutableStateFlow<List<OrderWithItems>>(emptyList())
    val filteredOrders: StateFlow<List<OrderWithItems>> = _filteredOrders

    // الإحصائيات (تتحدث بناءً على النتائج المفلترة فقط بعد الضغط على تحليل)
    val stats: StateFlow<ReportStats> = _filteredOrders.map { orders ->
        val completedOrders = orders.filter { it.order.order_status.equals("completed", ignoreCase = true) }
        val canceledOrders = orders.filter { it.order.order_status.equals("canceled", ignoreCase = true) }
        
        var transportation = 0.0
        var revenue = 0.0
        var costs = 0.0
        
        completedOrders.forEach { orderData ->
            transportation += orderData.order.delivery_fees
            // الإيرادات من المنتجات والتوصيل معاً
            revenue += orderData.order.delivery_fees 
            orderData.items.forEach { item ->
                revenue += (item.selling_price * item.quantity)
                costs += (item.purchase_price * item.quantity)
            }
        }
        
        ReportStats(
            totalCompletedOrders = completedOrders.size,
            totalCanceledOrders = canceledOrders.size,
            totalTransportation = transportation,
            totalCosts = costs,
            totalRevenue = revenue,
            netProfit = revenue - costs - transportation // الربح الصافي (بدون احتساب مبالغ التوصيل كربح للمتجر إن أردت)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportStats())

    fun searchOrders() {
        hasSearched.value = true 
        
        var currentList = allOrders.value

        // فلترة رقم الطلب
        if (orderNumber.value.isNotBlank()) {
            currentList = currentList.filter { it.order.order_number.toString().contains(orderNumber.value) }
        }
        // فلترة نوع الموديل
        if (vehicleModel.value.isNotBlank()) {
            currentList = currentList.filter { it.order.vehicle_model.contains(vehicleModel.value, ignoreCase = true) }
        }
        // فلترة حالة الطلب
        if (orderStatus.value.isNotBlank()) {
            currentList = currentList.filter { it.order.order_status.equals(orderStatus.value, ignoreCase = true) }
        }
        
        // فلترة التاجر أو القطعة
        if (merchantName.value.isNotBlank() || partName.value.isNotBlank()) {
            currentList = currentList.filter { orderData ->
                orderData.items.any { item ->
                    val matchMerchant = if (merchantName.value.isNotBlank()) item.provider_name.contains(merchantName.value, ignoreCase = true) else true
                    val matchPart = if (partName.value.isNotBlank()) item.part_name.contains(partName.value, ignoreCase = true) else true
                    matchMerchant && matchPart
                }
            }
        }

        // فلترة التاريخ
        if (isDateFilterEnabled.value && fromDate.value.isNotBlank() && toDate.value.isNotBlank()) {
            try {
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                val from = format.parse(fromDate.value)?.time ?: 0L
                val toParsed = format.parse(toDate.value)
                val to = if (toParsed != null) toParsed.time + 86399999L else Long.MAX_VALUE
                
                currentList = currentList.filter { orderData ->
                    val orderTime = when (val createdAt = orderData.order.created_at) {
                        is com.google.firebase.Timestamp -> createdAt.toDate().time
                        else -> 0L
                    }
                    orderTime in from..to
                }
            } catch (e: Exception) {
                // تجاهل خطأ التنسيق
            }
        }

        _filteredOrders.value = currentList
    }
}
