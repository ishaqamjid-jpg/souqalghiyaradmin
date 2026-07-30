package com.isaac.souqalghiyaradminnew.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
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
        val subscription = db.collection("orders").whereEqualTo("order_status", "pending").addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            if (snapshot != null) {
                val orderList = mutableListOf<OrderWithItems>()
                CoroutineScope(Dispatchers.IO).launch {
                    snapshot.documents.forEach { doc ->
                        val order = doc.toObject(Order::class.java)?.copy(order_id = doc.id)
                        if (order != null) {
                            try {
                                val itemsSnapshot = db.collection("orders").document(order.order_id).collection("items").get().await()
                                val items = itemsSnapshot.documents.mapNotNull { it.toObject(OrderItem::class.java)?.copy(item_id = it.id) }
                                orderList.add(OrderWithItems(order, items))
                            } catch (e: Exception) { e.printStackTrace() }
                        }
                    }
                    trySend(orderList.sortedByDescending { it.order.created_at.toString() }).isSuccess
                }
            }
        }
        awaitClose { subscription.remove() }
    }

    override fun getWaitingOrders(): Flow<List<OrderWithItems>> = callbackFlow {
        val subscription = db.collection("orders").whereEqualTo("order_status", "waiting for approvel").addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            if (snapshot != null) {
                val orderList = mutableListOf<OrderWithItems>()
                CoroutineScope(Dispatchers.IO).launch {
                    snapshot.documents.forEach { doc ->
                        val order = doc.toObject(Order::class.java)?.copy(order_id = doc.id)
                        if (order != null) {
                            try {
                                val itemsSnapshot = db.collection("orders").document(order.order_id).collection("items").get().await()
                                val items = itemsSnapshot.documents.mapNotNull { it.toObject(OrderItem::class.java)?.copy(item_id = it.id) }
                                orderList.add(OrderWithItems(order, items))
                            } catch (e: Exception) { e.printStackTrace() }
                        }
                    }
                    trySend(orderList.sortedByDescending { it.order.created_at.toString() }).isSuccess
                }
            }
        }
        awaitClose { subscription.remove() }
    }

    // الإضافة هنا: جلب الطلبات جاري التوصيل
    override fun getGoingOrders(): Flow<List<OrderWithItems>> = callbackFlow {
        val subscription = db.collection("orders").whereEqualTo("order_status", "going").addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            if (snapshot != null) {
                val orderList = mutableListOf<OrderWithItems>()
                CoroutineScope(Dispatchers.IO).launch {
                    snapshot.documents.forEach { doc ->
                        val order = doc.toObject(Order::class.java)?.copy(order_id = doc.id)
                        if (order != null) {
                            try {
                                val itemsSnapshot = db.collection("orders").document(order.order_id).collection("items").get().await()
                                val items = itemsSnapshot.documents.mapNotNull { it.toObject(OrderItem::class.java)?.copy(item_id = it.id) }
                                orderList.add(OrderWithItems(order, items))
                            } catch (e: Exception) { e.printStackTrace() }
                        }
                    }
                    trySend(orderList.sortedByDescending { it.order.created_at.toString() }).isSuccess
                }
            }
        }
        awaitClose { subscription.remove() }
    }

    override fun getCompletedOrders(): Flow<List<OrderWithItems>> = callbackFlow {
        val subscription = db.collection("orders").whereIn("order_status", listOf("completed", "canceled")).addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            if (snapshot != null) {
                val orderList = mutableListOf<OrderWithItems>()
                CoroutineScope(Dispatchers.IO).launch {
                    snapshot.documents.forEach { doc ->
                        val order = doc.toObject(Order::class.java)?.copy(order_id = doc.id)
                        if (order != null) {
                            try {
                                val itemsSnapshot = db.collection("orders").document(order.order_id).collection("items").get().await()
                                val items = itemsSnapshot.documents.mapNotNull { it.toObject(OrderItem::class.java)?.copy(item_id = it.id) }
                                orderList.add(OrderWithItems(order, items))
                            } catch (e: Exception) { e.printStackTrace() }
                        }
                    }
                    trySend(orderList.sortedByDescending { it.order.created_at.toString() }).isSuccess
                }
            }
        }
        awaitClose { subscription.remove() }
    }

    override suspend fun getOrdersByDateRange(status: String, startTimestamp: Long, endTimestamp: Long): List<OrderWithItems> {
        return try {
            val snapshot = db.collection("orders").whereEqualTo("order_status", status).get().await()
            val orderList = mutableListOf<OrderWithItems>()
            for (doc in snapshot.documents) {
                val order = doc.toObject(Order::class.java)?.copy(order_id = doc.id) ?: continue
                val orderTime = when (val createdAtRaw = doc.get("created_at")) {
                    is com.google.firebase.Timestamp -> createdAtRaw.toDate().time
                    is java.util.Date -> createdAtRaw.time
                    is Number -> createdAtRaw.toLong()
                    else -> 0L
                }
                if (orderTime in startTimestamp..endTimestamp) {
                    val itemsSnapshot = db.collection("orders").document(order.order_id).collection("items").get().await()
                    val items = itemsSnapshot.documents.mapNotNull { it.toObject(OrderItem::class.java)?.copy(item_id = it.id) }
                    orderList.add(OrderWithItems(order, items))
                }
            }
            orderList.sortedByDescending { it.order.created_at.toString() }
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun updateOrderItemAdminFields(
        orderId: String, itemId: String, purchasePrice: Double, sellingPrice: Double, providerName: String, invoiceNumber: String
    ): Result<Unit> {
        return try {
            db.collection("orders").document(orderId).collection("items").document(itemId).update(
                mapOf("purchase_price" to purchasePrice, "selling_price" to sellingPrice, "provider_name" to providerName, "invoice_number" to invoiceNumber)
            ).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override fun getAllOrdersForReports(): Flow<List<OrderWithItems>> = callbackFlow {
        val subscription = db.collection("orders").addSnapshotListener { snapshot, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            if (snapshot != null) {
                val orderList = mutableListOf<OrderWithItems>()
                CoroutineScope(Dispatchers.IO).launch {
                    snapshot.documents.forEach { doc ->
                        val order = doc.toObject(Order::class.java)?.copy(order_id = doc.id)
                        if (order != null) {
                            try {
                                val itemsSnapshot = db.collection("orders").document(order.order_id).collection("items").get().await()
                                val items = itemsSnapshot.documents.mapNotNull { it.toObject(OrderItem::class.java)?.copy(item_id = it.id) }
                                orderList.add(OrderWithItems(order, items))
                            } catch (e: Exception) { e.printStackTrace() }
                        }
                    }
                    trySend(orderList.sortedByDescending { it.order.created_at.toString() }).isSuccess
                }
            }
        }
        awaitClose { subscription.remove() }
    }

    override suspend fun deleteAdminAlarmByOrderNumber(orderNumber: Long): Result<Unit> {
        return try {
            val snapshot = db.collection("admin_alarm").whereEqualTo("order_number", orderNumber).get().await()
            for (doc in snapshot.documents) {
                db.collection("admin_alarm").document(doc.id).delete().await()
            }
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    override suspend fun updateOrderStatus(orderId: String, newStatus: String, deliveryFees: Double): Result<Unit> {
        return try {
            // 1. تحديث حالة الطلب
            db.collection("orders").document(orderId).update(
                mapOf("order_status" to newStatus, "delivery_fees" to deliveryFees)
            ).await()

            // 2. جلب بيانات الطلب للتعامل مع الإشعارات
            val orderSnapshot = db.collection("orders").document(orderId).get().await()
            val userId = orderSnapshot.getString("user_id") ?: ""
            val orderNumber = orderSnapshot.getLong("order_number") ?: 0L

            // 3. حذف إشعارات الإدارة (admin_alarm) المرتبطة بهذا الطلب (لأنه تم تسعيره)
            deleteAdminAlarmByOrderNumber(orderNumber)

            // 4. إنشاء تنبيه للعميل في جدول user_alarm
            if (newStatus == "waiting for approvel") {
                if (userId.isNotEmpty()) {
                    val userSnapshot = db.collection("users").document(userId).get().await()
                    val fcmToken = userSnapshot.getString("fcm_token") ?: ""

                    val userAlarmRef = db.collection("user_alarm").document()
                    val alarmData = hashMapOf(
                        "alarm_id" to userAlarmRef.id,
                        "date" to com.google.firebase.Timestamp.now(),
                        "order_number" to orderNumber,
                        "title" to "فاتورة جاهزة",
                        "message" to "تم تسعير طلبك رقم $orderNumber، يرجى مراجعته.",
                        "receiver_id" to userId,
                        "fcm_token" to fcmToken,
                        "isRead" to false
                    )
                    userAlarmRef.set(alarmData).await()
                }
            }
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }
    
    // الإضافة هنا: لإنهاء طلب "جاري التوصيل"
    override suspend fun finalizeOrder(orderId: String, orderNumber: Long, userId: String, isSuccess: Boolean, notes: String, incrementRejection: Boolean): Result<Unit> {
        return try {
            val newStatus = if (isSuccess) "completed" else "canceled"
            val updates = mutableMapOf<String, Any>("order_status" to newStatus)
            if (!isSuccess && notes.isNotEmpty()) {
                updates["approval_notes"] = notes 
            }
            
            db.collection("orders").document(orderId).update(updates).await()

            // رفع عداد الرفض إذا طلب الآدمن ذلك عند الإلغاء
            if (!isSuccess && incrementRejection && userId.isNotEmpty()) {
                db.collection("users").document(userId).update(
                    "number_of_rejections", FieldValue.increment(1.0)
                ).await()
            }

            // مسح اشعار جاري التوصيل من الإدارة
            deleteAdminAlarmByOrderNumber(orderNumber)

            // إنشاء إشعار للعميل بالنتيجة
            if (userId.isNotEmpty()) {
                val userSnapshot = db.collection("users").document(userId).get().await()
                val fcmToken = userSnapshot.getString("fcm_token") ?: ""

                val title = if (isSuccess) "تم التوصيل" else "تم إلغاء الطلب"
                val message = if (isSuccess) "تم توصيل طلبك رقم $orderNumber بنجاح." else "تم إلغاء طلبك رقم $orderNumber من قبل الإدارة."
                
                val userAlarmRef = db.collection("user_alarm").document()
                userAlarmRef.set(hashMapOf(
                    "alarm_id" to userAlarmRef.id,
                    "date" to com.google.firebase.Timestamp.now(),
                    "order_number" to orderNumber,
                    "title" to title,
                    "message" to message,
                    "receiver_id" to userId,
                    "fcm_token" to fcmToken,
                    "isRead" to false
                )).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getUnreadOrders(status: String): Flow<List<OrderWithItems>> = callbackFlow {
        val titleFilter = if (status == "canceled") "طلب مرفوض" else "طلب مكتمل"
        val subscription = db.collection("admin_alarm")
            .whereEqualTo("title", titleFilter)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }

                val orderNumbers = snapshot?.documents?.mapNotNull { it.getLong("order_number") }?.distinct() ?: emptyList()

                if (orderNumbers.isEmpty()) {
                    trySend(emptyList()).isSuccess
                    return@addSnapshotListener
                }

                val limitedNumbers = orderNumbers.take(10) 

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val ordersSnapshot = db.collection("orders")
                            .whereEqualTo("order_status", status)
                            .whereIn("order_number", limitedNumbers)
                            .get().await()

                        val orderList = mutableListOf<OrderWithItems>()
                        for (doc in ordersSnapshot.documents) {
                            val order = doc.toObject(Order::class.java)?.copy(order_id = doc.id)
                            if (order != null) {
                                val itemsSnapshot = db.collection("orders").document(order.order_id).collection("items").get().await()
                                val items = itemsSnapshot.documents.mapNotNull { it.toObject(OrderItem::class.java)?.copy(item_id = it.id) }
                                orderList.add(OrderWithItems(order, items))
                            }
                        }
                        trySend(orderList.sortedByDescending { it.order.created_at.toString() }).isSuccess
                    } catch (e: Exception) { e.printStackTrace() }
                }
            }
        awaitClose { subscription.remove() }
    }
}
