package com.isaac.souqalghiyaradminnew.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.isaac.souqalghiyaradminnew.domain.model.Order
import com.isaac.souqalghiyaradminnew.domain.model.OrderItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// --- كلاس مساعد لجمع الطلب مع قطعه ---
data class OrderWithItemsData(
    val order: Order,
    val items: List<OrderItem>
)

@HiltViewModel
class AdvancedOrdersViewModel @Inject constructor(
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchResult = MutableStateFlow<OrderWithItemsData?>(null)
    val searchResult = _searchResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // متغير لحفظ رقم هاتف العميل
    private val _clientPhone = MutableStateFlow("جاري الجلب...")
    val clientPhone = _clientPhone.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun searchOrderByNumber() {
        val number = _searchQuery.value.toLongOrNull() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _searchResult.value = null
            _clientPhone.value = "جاري الجلب..."
            try {
                // 1. جلب الطلب الأساسي من الفايربيز برقم الطلب
                val orderSnapshot = db.collection("orders")
                    .whereEqualTo("order_number", number)
                    .get().await()

                if (!orderSnapshot.isEmpty) {
                    val orderDoc = orderSnapshot.documents.first()
                    val order = orderDoc.toObject(Order::class.java)

                    if (order != null) {
                        // 2. جلب القطع المرتبطة بهذا الطلب
                        val itemsSnapshot = db.collection("order_items")
                            .whereEqualTo("order_id", order.order_id)
                            .get().await()

                        val itemsList = itemsSnapshot.documents.mapNotNull {
                            it.toObject(OrderItem::class.java)
                        }

                        _searchResult.value = OrderWithItemsData(order, itemsList)

                        // 3. جلب رقم هاتف العميل من جدول users
                        try {
                            val userSnapshot = db.collection("users")
                                .whereEqualTo("user_id", order.user_id)
                                .get().await()

                            if (!userSnapshot.isEmpty) {
                                _clientPhone.value = userSnapshot.documents.first().getString("phone_number") ?: "غير متوفر"
                            } else {
                                val doc = db.collection("users").document(order.user_id).get().await()
                                _clientPhone.value = doc.getString("phone_number") ?: "غير متوفر"
                            }
                        } catch (e: Exception) {
                            _clientPhone.value = "غير متوفر"
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateOrderAndItems(
        updatedOrder: Order,
        updatedItems: List<OrderItem>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // تحديث الطلب الأساسي
                db.collection("orders").document(updatedOrder.order_id).set(updatedOrder).await()

                // تحديث كل قطعة في الطلب
                for (item in updatedItems) {
                    if (item.item_id.isNotEmpty()) {
                        db.collection("order_items").document(item.item_id).set(item).await()
                    }
                }

                _searchResult.value = OrderWithItemsData(updatedOrder, updatedItems)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "حدث خطأ غير معروف")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteOrder(orderId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // 1. جلب وحذف جميع القطع المرتبطة بهذا الطلب
                val itemsSnapshot = db.collection("order_items")
                    .whereEqualTo("order_id", orderId)
                    .get().await()

                for (document in itemsSnapshot.documents) {
                    db.collection("order_items").document(document.id).delete().await()
                }

                // 2. حذف الطلب الأساسي
                db.collection("orders").document(orderId).delete().await()

                _searchResult.value = null
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}