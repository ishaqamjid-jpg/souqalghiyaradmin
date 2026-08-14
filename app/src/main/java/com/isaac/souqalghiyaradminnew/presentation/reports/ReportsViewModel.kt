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

    // جلب جميع الطلبات الأساسية
    private val allOrders = repository.getAllOrdersForReports()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // ==========================================
    // 1. قسم الإحصائيات (Stats Section)
    // ==========================================
    val isStatsDateFilterEnabled = MutableStateFlow(false)
    val statsFromDate = MutableStateFlow("")
    val statsToDate = MutableStateFlow("")
    val hasAnalyzedStats = MutableStateFlow(false)

    private val _statsOrders = MutableStateFlow<List<OrderWithItems>>(emptyList())
    
    val stats: StateFlow<ReportStats> = _statsOrders.map { orders ->
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
            netProfit = revenue - costs - transportation
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportStats())

    fun analyzeStats() {
        hasAnalyzedStats.value = true 
        var currentList = allOrders.value

        if (isStatsDateFilterEnabled.value && statsFromDate.value.isNotBlank() && statsToDate.value.isNotBlank()) {
            try {
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                val from = format.parse(statsFromDate.value)?.time ?: 0L
                val toParsed = format.parse(statsToDate.value)
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
        _statsOrders.value = currentList
    }


    // ==========================================
    // 2. قسم التقارير وسجل الطلبات (Reports Section)
    // ==========================================
    val reportMerchantName = MutableStateFlow("")
    val reportPartName = MutableStateFlow("")
    val reportOrderNumber = MutableStateFlow("")
    val reportOrderStatus = MutableStateFlow("")
    val hasSearchedReports = MutableStateFlow(false)

    private val _reportFilteredOrders = MutableStateFlow<List<OrderWithItems>>(emptyList())
    val reportFilteredOrders: StateFlow<List<OrderWithItems>> = _reportFilteredOrders

    fun searchReports() {
        hasSearchedReports.value = true 
        var currentList = allOrders.value

        // فلترة رقم الطلب
        if (reportOrderNumber.value.isNotBlank()) {
            currentList = currentList.filter { it.order.order_number.toString().contains(reportOrderNumber.value) }
        }
        // فلترة حالة الطلب
        if (reportOrderStatus.value.isNotBlank()) {
            currentList = currentList.filter { it.order.order_status.equals(reportOrderStatus.value, ignoreCase = true) }
        }
        // فلترة التاجر أو القطعة
        if (reportMerchantName.value.isNotBlank() || reportPartName.value.isNotBlank()) {
            currentList = currentList.filter { orderData ->
                orderData.items.any { item ->
                    val matchMerchant = if (reportMerchantName.value.isNotBlank()) item.provider_name.contains(reportMerchantName.value, ignoreCase = true) else true
                    val matchPart = if (reportPartName.value.isNotBlank()) item.part_name.contains(reportPartName.value, ignoreCase = true) else true
                    matchMerchant && matchPart
                }
            }
        }
        _reportFilteredOrders.value = currentList
    }
}
