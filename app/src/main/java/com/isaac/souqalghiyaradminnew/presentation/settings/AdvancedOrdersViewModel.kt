package com.isaac.souqalghiyaradminnew.presentation.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
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
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

// كلاس مساعد لجمع الطلب مع قطعه
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

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun searchOrderByNumber() {
        val number = _searchQuery.value.toLongOrNull() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            _searchResult.value = null
            try {
                // 1. جلب الطلب الأساسي من الفايربيز
                val orderSnapshot = db.collection("orders")
                    .whereEqualTo("order_number", number)
                    .get().await()

                if (!orderSnapshot.isEmpty) {
                    val order = orderSnapshot.documents.first().toObject(Order::class.java)
                    
                    if (order != null) {
                        // 2. جلب القطع المرتبطة بهذا الطلب
                        val itemsSnapshot = db.collection("order_items")
                            .whereEqualTo("order_id", order.order_id)
                            .get().await()
                            
                        val itemsList = itemsSnapshot.documents.mapNotNull { 
                            it.toObject(OrderItem::class.java) 
                        }

                        _searchResult.value = OrderWithItemsData(order, itemsList)
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

    // دالة الحذف المعدلة: تقوم بحذف القطع أولاً ثم الطلب
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedOrdersScreen(
    viewModel: AdvancedOrdersViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val query by viewModel.searchQuery.collectAsState()
    val result by viewModel.searchResult.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("بحث متقدم بالطلبات", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, contentDescription = "", tint = Color.White) }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E))
                )
            },
            containerColor = Color(0xFF121212)
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                // خانة البحث
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        label = { Text("أدخل رقم الطلب (Order Number)", color = Color.Gray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White, 
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFE91E63), 
                            unfocusedBorderColor = Color.Gray,
                            cursorColor = Color(0xFFE91E63)
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { viewModel.searchOrderByNumber() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                        modifier = Modifier.height(55.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "بحث", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = Color(0xFFE91E63))
                } else if (result != null) {
                    // عرض نموذج تعديل الطلب والقطع
                    OrderEditForm(
                        data = result!!,
                        onSave = { updatedOrder, updatedItems ->
                            viewModel.updateOrderAndItems(
                                updatedOrder,
                                updatedItems,
                                onSuccess = { Toast.makeText(context, "تم الحفظ بنجاح!", Toast.LENGTH_SHORT).show() },
                                onError = { Toast.makeText(context, "خطأ: $it", Toast.LENGTH_LONG).show() }
                            )
                        },
                        onDelete = { orderId ->
                            viewModel.deleteOrder(
                                orderId,
                                onSuccess = { Toast.makeText(context, "تم حذف الطلب والقطع بنجاح!", Toast.LENGTH_SHORT).show() }
                            )
                        }
                    )
                } else if (query.isNotEmpty()) {
                    Text("لا يوجد طلب بهذا الرقم أو ابحث للبدء.", color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally))
                }
            }
        }
    }
}

@Composable
fun OrderEditForm(
    data: OrderWithItemsData, 
    onSave: (Order, List<OrderItem>) -> Unit, 
    onDelete: (String) -> Unit
) {
    val order = data.order
    
    // متغيرات الطلب (للقراءة فقط - للقراءة ولا يتم تعديلها)
    val readOnlyColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.LightGray,
        unfocusedTextColor = Color.LightGray,
        focusedBorderColor = Color.DarkGray,
        unfocusedBorderColor = Color.DarkGray,
        focusedLabelColor = Color.Gray,
        unfocusedLabelColor = Color.Gray,
        disabledTextColor = Color.LightGray,
        disabledBorderColor = Color.DarkGray,
        disabledLabelColor = Color.Gray
    )

    // متغيرات الطلب القابلة للتعديل
    var brandName by remember(order.order_id) { mutableStateOf(order.brand_name) }
    var vehicleName by remember(order.order_id) { mutableStateOf(order.vehicle_name) }
    var vehicleModel by remember(order.order_id) { mutableStateOf(order.vehicle_model) }
    var manufacture by remember(order.order_id) { mutableStateOf(order.manufacture) }
    var vinNumber by remember(order.order_id) { mutableStateOf(order.vin_number) }
    var location by remember(order.order_id) { mutableStateOf(order.location) }
    var deliveryLocation by remember(order.order_id) { mutableStateOf(order.delivery_location) }
    var deliveryFees by remember(order.order_id) { mutableStateOf(order.delivery_fees.toString()) }
    var orderStatus by remember(order.order_id) { mutableStateOf(order.order_status) }
    var approvalNotes by remember(order.order_id) { mutableStateOf(order.approval_notes) }
    var disapprovalNotes by remember(order.order_id) { mutableStateOf(order.disapproval_notes) }

    // قائمة القطع القابلة للتعديل
    var itemsList by remember(order.order_id) { mutableStateOf(data.items) }

    val customTextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedBorderColor = Color(0xFFE91E63),
        unfocusedBorderColor = Color.Gray,
        focusedLabelColor = Color(0xFFE91E63),
        unfocusedLabelColor = Color.Gray,
        cursorColor = Color(0xFFE91E63)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- قسم بيانات الطلب الأساسية ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("بيانات الطلب الأساسية:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            
            // حقول الثوابت (للقراءة فقط)
            OutlinedTextField(value = order.order_id, onValueChange = {}, readOnly = true, enabled = false, label = { Text("معرف الطلب (Order ID)") }, modifier = Modifier.fillMaxWidth(), colors = readOnlyColors)
            OutlinedTextField(value = order.user_id, onValueChange = {}, readOnly = true, enabled = false, label = { Text("معرف المستخدم (User ID)") }, modifier = Modifier.fillMaxWidth(), colors = readOnlyColors)
            OutlinedTextField(value = order.order_number.toString(), onValueChange = {}, readOnly = true, enabled = false, label = { Text("رقم الطلب (Order Number)") }, modifier = Modifier.fillMaxWidth(), colors = readOnlyColors)
            
            val dateString = order.created_at?.toDate()?.let { 
                SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale.ENGLISH).format(it) 
            } ?: "غير محدد"
            OutlinedTextField(value = dateString, onValueChange = {}, readOnly = true, enabled = false, label = { Text("تاريخ الإنشاء") }, modifier = Modifier.fillMaxWidth(), colors = readOnlyColors)

            HorizontalDivider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 8.dp))

            // الحقول القابلة للتعديل
            OutlinedTextField(value = brandName, onValueChange = { brandName = it }, label = { Text("الماركة") }, modifier = Modifier.fillMaxWidth(), colors = customTextFieldColors)
            OutlinedTextField(value = vehicleName, onValueChange = { vehicleName = it }, label = { Text("المركبة") }, modifier = Modifier.fillMaxWidth(), colors = customTextFieldColors)
            OutlinedTextField(value = vehicleModel, onValueChange = { vehicleModel = it }, label = { Text("الموديل") }, modifier = Modifier.fillMaxWidth(), colors = customTextFieldColors)
            OutlinedTextField(value = manufacture, onValueChange = { manufacture = it }, label = { Text("بلد الصنع") }, modifier = Modifier.fillMaxWidth(), colors = customTextFieldColors)
            OutlinedTextField(value = vinNumber, onValueChange = { vinNumber = it }, label = { Text("رقم القعادة") }, modifier = Modifier.fillMaxWidth(), colors = customTextFieldColors)
            OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("المحافظة") }, modifier = Modifier.fillMaxWidth(), colors = customTextFieldColors)
            OutlinedTextField(value = deliveryLocation, onValueChange = { deliveryLocation = it }, label = { Text("العنوان") }, modifier = Modifier.fillMaxWidth(), colors = customTextFieldColors)
            OutlinedTextField(value = deliveryFees, onValueChange = { deliveryFees = it }, label = { Text("رسوم التوصيل") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = customTextFieldColors)
            OutlinedTextField(value = orderStatus, onValueChange = { orderStatus = it }, label = { Text("حالة الطلب") }, modifier = Modifier.fillMaxWidth(), colors = customTextFieldColors)
            OutlinedTextField(value = approvalNotes, onValueChange = { approvalNotes = it }, label = { Text("ملاحظات الموافقة") }, modifier = Modifier.fillMaxWidth(), colors = customTextFieldColors)
            OutlinedTextField(value = disapprovalNotes, onValueChange = { disapprovalNotes = it }, label = { Text("سبب الرفض") }, modifier = Modifier.fillMaxWidth(), colors = customTextFieldColors)
        }

        // --- قسم بيانات القطع ---
        Text("القطع المطلوبة (${itemsList.size} قطع):", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        
        itemsList.forEachIndexed { index, item ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2C2C2C), RoundedCornerShape(8.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("قطعة رقم ${index + 1}", color = Color(0xFFE91E63), fontWeight = FontWeight.Bold)
                
                OutlinedTextField(
                    value = item.item_id, onValueChange = {}, readOnly = true, enabled = false, 
                    label = { Text("معرف القطعة (Item ID)") }, modifier = Modifier.fillMaxWidth(), colors = readOnlyColors
                )
                OutlinedTextField(
                    value = item.part_name, 
                    onValueChange = { newValue -> itemsList = itemsList.toMutableList().apply { this[index] = item.copy(part_name = newValue) } }, 
                    label = { Text("اسم القطعة") }, modifier = Modifier.fillMaxWidth(), colors = customTextFieldColors
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = item.quantity.toString(), 
                        onValueChange = { newValue -> itemsList = itemsList.toMutableList().apply { this[index] = item.copy(quantity = newValue.toIntOrNull() ?: 1) } }, 
                        label = { Text("الكمية") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = customTextFieldColors
                    )
                    OutlinedTextField(
                        value = item.quality_type, 
                        onValueChange = { newValue -> itemsList = itemsList.toMutableList().apply { this[index] = item.copy(quality_type = newValue) } }, 
                        label = { Text("الجودة") }, modifier = Modifier.weight(1f), colors = customTextFieldColors
                    )
                }
                OutlinedTextField(
                    value = item.description, 
                    onValueChange = { newValue -> itemsList = itemsList.toMutableList().apply { this[index] = item.copy(description = newValue) } }, 
                    label = { Text("وصف القطعة (جهة/مكان)") }, modifier = Modifier.fillMaxWidth(), colors = customTextFieldColors
                )
                OutlinedTextField(
                    value = item.comments, 
                    onValueChange = { newValue -> itemsList = itemsList.toMutableList().apply { this[index] = item.copy(comments = newValue) } }, 
                    label = { Text("ملاحظات العميل") }, modifier = Modifier.fillMaxWidth(), colors = customTextFieldColors
                )
                
                HorizontalDivider(color = Color.Gray, modifier = Modifier.padding(vertical = 4.dp))
                
                OutlinedTextField(
                    value = item.provider_name, 
                    onValueChange = { newValue -> itemsList = itemsList.toMutableList().apply { this[index] = item.copy(provider_name = newValue) } }, 
                    label = { Text("اسم الموفر") }, modifier = Modifier.fillMaxWidth(), colors = customTextFieldColors
                )
                OutlinedTextField(
                    value = item.invoice_number, 
                    onValueChange = { newValue -> itemsList = itemsList.toMutableList().apply { this[index] = item.copy(invoice_number = newValue) } }, 
                    label = { Text("رقم فاتورة الموفر") }, modifier = Modifier.fillMaxWidth(), colors = customTextFieldColors
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = item.purchase_price.toString(), 
                        onValueChange = { newValue -> itemsList = itemsList.toMutableList().apply { this[index] = item.copy(purchase_price = newValue.toDoubleOrNull() ?: 0.0) } }, 
                        label = { Text("سعر الشراء") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = customTextFieldColors
                    )
                    OutlinedTextField(
                        value = item.selling_price.toString(), 
                        onValueChange = { newValue -> itemsList = itemsList.toMutableList().apply { this[index] = item.copy(selling_price = newValue.toDoubleOrNull() ?: 0.0) } }, 
                        label = { Text("سعر البيع") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = customTextFieldColors
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- أزرار الحفظ والحذف ---
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(
                onClick = { onDelete(order.order_id) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("حذف نهائياً")
            }

            Button(
                onClick = {
                    val updatedOrder = order.copy(
                        brand_name = brandName,
                        vehicle_name = vehicleName,
                        vehicle_model = vehicleModel,
                        manufacture = manufacture,
                        vin_number = vinNumber,
                        location = location,
                        delivery_location = deliveryLocation,
                        delivery_fees = deliveryFees.toDoubleOrNull() ?: 0.0,
                        order_status = orderStatus,
                        approval_notes = approvalNotes,
                        disapproval_notes = disapprovalNotes
                    )
                    onSave(updatedOrder, itemsList)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("حفظ التعديلات")
            }
        }
    }
}
