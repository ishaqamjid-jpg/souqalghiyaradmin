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

    // ... (دوال جلب الطلبات تبقى كما هي: getPendingOrders, getWaitingOrders الخ)

    override suspend fun updateOrderStatus(orderId: String, newStatus: String, deliveryFees: Double): Result<Unit> {
        return try {
            // 1. تحديث حالة الطلب ورسوم التوصيل
            db.collection("orders").document(orderId).update(
                mapOf(
                    "order_status" to newStatus,
                    "delivery_fees" to deliveryFees
                )
            ).await()

            // 2. إذا كانت الحالة "بانتظار الموافقة"، نقوم بإنشاء إشعار للعميل في جدول user_alarm
            if (newStatus == "waiting for approvel") {
                // جلب تفاصيل الطلب لمعرفة الـ user_id ورقم الطلب
                val orderSnapshot = db.collection("orders").document(orderId).get().await()
                val userId = orderSnapshot.getString("user_id") ?: ""
                val orderNumber = orderSnapshot.getLong("order_number") ?: 0L

                if (userId.isNotEmpty()) {
                    val userAlarmRef = db.collection("user_alarm").document()
                    val alarmData = hashMapOf(
                        "alarm_id" to userAlarmRef.id,
                        "date" to com.google.firebase.Timestamp.now(),
                        "order_number" to orderNumber,
                        "title" to "فاتورة جاهزة",
                        "message" to "تم تسعير طلبك رقم $orderNumber، يرجى مراجعته والموافقة عليه.",
                        "receiver_id" to userId,
                        "fcm_token" to "", // يتم استخدام الـ Cloud Functions لإرسال الإشعار أو جلبه عبر الـ receiver_id
                        "isRead" to false
                    )
                    userAlarmRef.set(alarmData).await()
                }
            }

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

    override suspend fun deleteAdminAlarmByOrderNumber(orderNumber: Long): Result<Unit> {
        return try {
            val snapshot = db.collection("admin_alarm")
                .whereEqualTo("order_number", orderNumber)
                .get()
                .await()
                
            for (doc in snapshot.documents) {
                db.collection("admin_alarm").document(doc.id).delete().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
