package com.isaac.souqalghiyaradminnew.domain.repository

import com.isaac.souqalghiyaradminnew.domain.model.Order
import com.isaac.souqalghiyaradminnew.domain.model.OrderItem
import com.isaac.souqalghiyaradminnew.presentation.settings.OrderWithItemsData

interface AdvancedOrdersRepository {
    suspend fun searchOrderByNumber(orderNumber: Long): OrderWithItemsData?
    suspend fun getClientPhone(userId: String): String
    suspend fun updateOrderAndItems(order: Order, items: List<OrderItem>)
    suspend fun deleteOrder(orderId: String)
}
