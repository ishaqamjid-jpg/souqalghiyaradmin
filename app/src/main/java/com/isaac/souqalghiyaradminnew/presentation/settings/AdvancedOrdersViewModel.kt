package com.isaac.souqalghiyaradminnew.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isaac.souqalghiyaradminnew.domain.model.Order
import com.isaac.souqalghiyaradminnew.domain.model.OrderItem
import com.isaac.souqalghiyaradminnew.domain.repository.AdvancedOrdersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// --- كلاس مساعد لجمع الطلب مع قطعه ---
data class OrderWithItemsData(
    val order: Order,
    val items: List<OrderItem>
)

@HiltViewModel
class AdvancedOrdersViewModel @Inject constructor(
    private val repository: AdvancedOrdersRepository // حقن الـ Repository بدلاً من FirebaseFirestore
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchResult = MutableStateFlow<OrderWithItemsData?>(null)
    val searchResult = _searchResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

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
                // جلب الطلب وقطعه عبر المستودع
                val resultData = repository.searchOrderByNumber(number)
                
                if (resultData != null) {
                    _searchResult.value = resultData
                    // جلب رقم الهاتف
                    _clientPhone.value = repository.getClientPhone(resultData.order.user_id)
                } else {
                    _clientPhone.value = "غير متوفر"
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
                
                // التحديث عبر المستودع
                repository.updateOrderAndItems(updatedOrder, updatedItems)
                
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
                
                // الحذف عبر المستودع
                repository.deleteOrder(orderId)
                
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
