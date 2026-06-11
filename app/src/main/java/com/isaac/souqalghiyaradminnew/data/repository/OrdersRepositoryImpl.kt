package com.isaac.souqalghiyaradminnew.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.isaac.souqalghiyaradminnew.domain.model.Order
import com.isaac.souqalghiyaradminnew.domain.model.OrderItem
import com.isaac.souqalghiyaradminnew.domain.model.OrderWithItems
import com.isaac.souqalghiyaradminnew.domain.repository.OrdersRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class OrdersRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : OrdersRepository {

    override fun getPendingOrders(): Flow<List<OrderWithItems>> = callbackFlow {
        val subscription = db.collection("orders")
            .whereEqualTo("order_status", "pending")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val orderList = mutableListOf<OrderWithItems>()
                    if (snapshot.isEmpty) {
                        trySend(emptyList()).isSuccess
                        return@addSnapshotListener
                    }
                    
                    // استخدام Coroutine لجلب القطع الفرعية بداخل الـ Listener بشكل آمن
                    CoroutineScope(Dispatchers.IO).launch {
                        snapshot.documents.forEach { doc ->
                            val order = doc.toObject(Order::class.java)?.copy(order_id = doc.id)
                            if (order != null) {
                                try {
                                    val itemsSnapshot = db.collection("orders").document(order.order_id).collection("items").get().await()
                                    val items = itemsSnapshot.documents.mapNotNull { itemDoc ->
                                        itemDoc.toObject(OrderItem::class.java)?.copy(item_id = itemDoc.id)
                                    }
                                    orderList.add(OrderWithItems(order, items))
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                        trySend(orderList.sortedByDescending { it.order.created_at }).isSuccess
                    }
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun getCompletedOrders(): Flow<List<OrderWithItems>> = callbackFlow {
        val subscription = db.collection("orders")
            .whereIn("order_status", listOf("completed", "canceled")) // جلب المنتهية والملغاة للتقارير
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val orderList = mutableListOf<OrderWithItems>()
                    if (snapshot.isEmpty) {
                        trySend(emptyList()).isSuccess
                        return@addSnapshotListener
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        snapshot.documents.forEach { doc ->
                            val order = doc.toObject(Order::class.java)?.copy(order_id = doc.id)
                            if (order != null) {
                                try {
                                    val itemsSnapshot = db.collection("orders").document(order.order_id).collection("items").get().await()
                                    val items = itemsSnapshot.documents.mapNotNull { itemDoc ->
                                        itemDoc.toObject(OrderItem::class.java)?.copy(item_id = itemDoc.id)
                                    }
                                    orderList.add(OrderWithItems(order, items))
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                        trySend(orderList.sortedByDescending { it.order.created_at }).isSuccess
                    }
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun updateOrderStatus(orderId: String, newStatus: String, deliveryFees: Double): Result<Unit> {
        return try {
            db.collection("orders").document(orderId).update(
                mapOf(
                    "order_status" to newStatus,
                    "delivery_fees" to deliveryFees
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateOrderItemAdminFields(
        orderId: String,
        itemId: String,
        purchasePrice: Double,
        sellingPrice: Double,
        providerName: String,
        invoiceNumber: String
    ): Result<Unit> {
        return try {
            db.collection("orders").document(orderId).collection("items").document(itemId).update(
                mapOf(
                    "purchase_price" to purchasePrice,
                    "selling_price" to sellingPrice,
                    "provider_name" to providerName,
                    "invoice_number" to invoiceNumber
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
