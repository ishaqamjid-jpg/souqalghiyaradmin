package com.isaac.souqalghiyaradminnew.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.isaac.souqalghiyaradminnew.domain.model.Order
import com.isaac.souqalghiyaradminnew.domain.model.OrderItem
import com.isaac.souqalghiyaradminnew.domain.repository.AdvancedOrdersRepository
import com.isaac.souqalghiyaradminnew.presentation.settings.OrderWithItemsData
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AdvancedOrdersRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : AdvancedOrdersRepository {

    override suspend fun searchOrderByNumber(orderNumber: Long): OrderWithItemsData? {
        val orderSnapshot = db.collection("orders")
            .whereEqualTo("order_number", orderNumber)
            .get().await()

        if (orderSnapshot.isEmpty) return null

        val orderDoc = orderSnapshot.documents.first()
        val order = orderDoc.toObject(Order::class.java)?.copy(order_id = orderDoc.id) ?: return null

        // جلب القطع من الـ Subcollection
        val itemsSnapshot = db.collection("orders")
            .document(order.order_id)
            .collection("items")
            .get().await()

        val itemsList = itemsSnapshot.documents.mapNotNull {
            it.toObject(OrderItem::class.java)?.copy(item_id = it.id)
        }

        return OrderWithItemsData(order, itemsList)
    }

    override suspend fun getClientPhone(userId: String): String {
        return try {
            val userSnapshot = db.collection("users")
                .whereEqualTo("user_id", userId)
                .get().await()

            if (!userSnapshot.isEmpty) {
                userSnapshot.documents.first().getString("phone_number") ?: "غير متوفر"
            } else {
                val doc = db.collection("users").document(userId).get().await()
                doc.getString("phone_number") ?: "غير متوفر"
            }
        } catch (e: Exception) {
            "غير متوفر"
        }
    }

    override suspend fun updateOrderAndItems(order: Order, items: List<OrderItem>) {
        // تحديث الطلب الأساسي
        db.collection("orders").document(order.order_id).set(order).await()

        // تحديث كل قطعة في الطلب بداخل مسارها الصحيح
        for (item in items) {
            if (item.item_id.isNotEmpty()) {
                db.collection("orders")
                    .document(order.order_id)
                    .collection("items")
                    .document(item.item_id)
                    .set(item).await()
            }
        }
    }

    override suspend fun deleteOrder(orderId: String) {
        // 1. جلب وحذف جميع القطع المرتبطة بهذا الطلب أولاً
        val itemsSnapshot = db.collection("orders")
            .document(orderId)
            .collection("items")
            .get().await()

        for (document in itemsSnapshot.documents) {
            document.reference.delete().await()
        }

        // 2. حذف الطلب الأساسي
        db.collection("orders").document(orderId).delete().await()
    }
}
