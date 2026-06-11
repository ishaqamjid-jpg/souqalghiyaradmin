package com.isaac.souqalghiyaradminnew.presentation.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaac.souqalghiyaradminnew.domain.repository.OrdersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class ReportStats(
    val totalCompletedOrders: Int = 0,
    val totalRevenue: Double = 0.0,
    val totalCosts: Double = 0.0,
    val netProfit: Double = 0.0
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val repository: OrdersRepository
) : ViewModel() {
    
    // جلب الطلبات المنتهية (completed) والملغاة (canceled) من المستودع
    val stats: StateFlow<ReportStats> = repository.getCompletedOrders()
        .map { ordersList ->
            // نأخذ فقط الطلبات المكتملة للحسابات المالية
            val completedOrders = ordersList.filter { it.order.order_status == "completed" }
            
            var revenue = 0.0
            var costs = 0.0
            
            completedOrders.forEach { orderData ->
                // إضافة رسوم التوصيل للإيرادات
                revenue += orderData.order.delivery_fees
                
                // حساب إيرادات وتكاليف القطع
                orderData.items.forEach { item ->
                    revenue += (item.selling_price * item.quantity)
                    costs += (item.purchase_price * item.quantity)
                }
            }
            
            ReportStats(
                totalCompletedOrders = completedOrders.size,
                totalRevenue = revenue,
                totalCosts = costs,
                netProfit = revenue - costs
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportStats())
}
