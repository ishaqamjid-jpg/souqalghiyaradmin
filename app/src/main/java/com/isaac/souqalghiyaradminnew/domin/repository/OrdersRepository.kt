package com.isaac.souqalghiyaradminnew.domain.repository

import com.isaac.souqalghiyaradminnew.domain.model.OrderWithItems
import kotlinx.coroutines.flow.Flow

interface OrdersRepository {
    // جلب الطلبات المعلقة (لعرضها للآدمن لتسعيرها)
    fun getPendingOrders(): Flow<List<OrderWithItems>>
    
    // جلب الطلبات المنتهية (لعرضها في التقارير)
    fun getCompletedOrders(): Flow<List<OrderWithItems>>

    // تحديث حالة الطلب الرئيسية ورسوم التوصيل
    suspend fun updateOrderStatus(orderId: String, newStatus: String, deliveryFees: Double): Result<Unit>

    // تحديث بيانات القطعة الواحدة من قبل الآدمن (تسعير القطع، المورد، الفاتورة)
    suspend fun updateOrderItemAdminFields(
        orderId: String,
        itemId: String,
        purchasePrice: Double,
        sellingPrice: Double,
        providerName: String,
        invoiceNumber: String
    ): Result<Unit>
}
