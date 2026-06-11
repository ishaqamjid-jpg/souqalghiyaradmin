package com.isaac.souqalghiyaradminnew.presentation.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaac.souqalghiyaradminnew.domain.model.OrderWithItems
import com.isaac.souqalghiyaradminnew.domain.repository.OrdersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// كلاس مساعد لجمع بيانات التسعير لكل قطعة من الواجهة
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

    // جلب الطلبات المعلقة (مدمجة مع قطعها) مباشرة من المستودع
    val pendingOrders: StateFlow<List<OrderWithItems>> = repository.getPendingOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // دالة الموافقة على الطلب (تحفظ أسعار القطع وتحدث حالة الطلب)
    fun approveOrder(
        orderId: String,
        deliveryFees: Double,
        itemsPricing: List<ItemAdminPricing>
    ) {
        viewModelScope.launch {
            // 1. تحديث بيانات كل قطعة (أسعار وتجار)
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

            // 2. تحديث حالة الطلب ليصبح مسعّراً / جاري التوصيل، مع حفظ رسوم التوصيل
            // افترضت أن الحالة ستصبح 'ongoing' بمجرد الاعتماد من الإدارة
            repository.updateOrderStatus(orderId, "ongoing", deliveryFees)
        }
    }

    // الدالة المفقودة: رفض وإلغاء الطلب
    fun rejectOrder(orderId: String) {
        viewModelScope.launch {
            // تغيير حالة الطلب إلى 'canceled' ورسوم التوصيل 0.0
            repository.updateOrderStatus(orderId, "canceled", 0.0)
        }
    }
}