package com.isaac.souqalghiyaradminnew.domain.repository

import com.isaac.souqalghiyaradminnew.domain.model.OrderWithItems
import kotlinx.coroutines.flow.Flow

interface OrdersRepository {
    fun getPendingOrders(): Flow<List<OrderWithItems>>
    fun getWaitingOrders(): Flow<List<OrderWithItems>>
    fun getCompletedOrders(): Flow<List<OrderWithItems>>
    suspend fun getOrdersByDateRange(status: String, startTimestamp: Long, endTimestamp: Long): List<OrderWithItems>
    suspend fun updateOrderStatus(orderId: String, newStatus: String, deliveryFees: Double): Result<Unit>
    suspend fun updateOrderItemAdminFields(
        orderId: String, itemId: String, purchasePrice: Double, sellingPrice: Double, providerName: String, invoiceNumber: String
    ): Result<Unit>
    fun getAllOrdersForReports(): Flow<List<OrderWithItems>>
    suspend fun deleteAdminAlarmByOrderNumber(orderNumber: Long): Result<Unit>
    
    // الدالة الجديدة المضافة هنا:
    fun getUnreadOrders(status: String): Flow<List<OrderWithItems>>
}
