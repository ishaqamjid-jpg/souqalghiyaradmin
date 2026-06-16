package com.isaac.souqalghiyaradminnew.domain.repository

import com.isaac.souqalghiyaradminnew.domain.model.OrderWithItems
import kotlinx.coroutines.flow.Flow

interface OrdersRepository {
    // 1. جلب الطلبات المعلقة (لعرضها للآدمن لتسعيرها)
    fun getPendingOrders(): Flow<List<OrderWithItems>>

    // 2. جلب الطلبات قيد الموافقة (تم تسعيرها بانتظار موافقة العميل)
    fun getWaitingOrders(): Flow<List<OrderWithItems>>

    // 3. جلب الطلبات المنتهية (لعرضها في التقارير) - الدالة القديمة
    fun getCompletedOrders(): Flow<List<OrderWithItems>>

    // 4. جلب الطلبات المرفوضة أو المكتملة بناءً على التاريخ (للفلترة)
    suspend fun getOrdersByDateRange(status: String, startTimestamp: Long, endTimestamp: Long): List<OrderWithItems>

    // 5. تحديث حالة الطلب الرئيسية ورسوم التوصيل
    suspend fun updateOrderStatus(orderId: String, newStatus: String, deliveryFees: Double): Result<Unit>

    // 6. تحديث بيانات القطعة الواحدة من قبل الآدمن (تسعير القطع، المورد، الفاتورة)
    suspend fun updateOrderItemAdminFields(
        orderId: String,
        itemId: String,
        purchasePrice: Double,
        sellingPrice: Double,
        providerName: String,
        invoiceNumber: String
    ): Result<Unit>
}