package com.isaac.souqalghiyaradminnew.presentation.orders

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.isaac.souqalghiyaradminnew.domain.model.OrderWithItems
import kotlinx.coroutines.flow.StateFlow
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersManagementScreen(
    viewModel: OrdersViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("المعلقة", "قيد الموافقة", "المرفوضة", "المكتملة")

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        title = { Text("إدارة الطلبات", color = Color.White, fontWeight = FontWeight.Bold) },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D1B6D))
                    )
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color(0xFF0D1B6D),
                        contentColor = Color.White,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = Color(0xFF4CAF50),
                                height = 3.dp
                            )
                        }
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = { Text(title, fontSize = 14.sp, fontWeight = if(selectedTab == index) FontWeight.Bold else FontWeight.Normal) },
                                unselectedContentColor = Color.LightGray,
                                selectedContentColor = Color.White
                            )
                        }
                    }
                }
            },
            containerColor = Color(0xFFF5F5F5)
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                when (selectedTab) {
                    0 -> PendingOrdersSection(viewModel)
                    1 -> WaitingOrdersSection(viewModel)
                    2 -> HistoricalOrdersSection(viewModel, "canceled", viewModel.canceledOrders, viewModel.unreadCanceledOrders)
                    3 -> HistoricalOrdersSection(viewModel, "completed", viewModel.completedOrders, viewModel.unreadCompletedOrders)
                }
            }
        }
    }
}

@Composable
fun PendingOrdersSection(viewModel: OrdersViewModel) {
    val orders by viewModel.pendingOrders.collectAsState()
    OrdersList(orders = orders, viewModel = viewModel, isEditable = true, showPdfExport = true)
}

@Composable
fun WaitingOrdersSection(viewModel: OrdersViewModel) {
    val orders by viewModel.waitingOrders.collectAsState()
    OrdersList(orders = orders, viewModel = viewModel, isEditable = false, showPdfExport = true)
}

@Composable
fun HistoricalOrdersSection(
    viewModel: OrdersViewModel,
    status: String,
    historicalOrdersFlow: StateFlow<List<OrderWithItems>>,
    unreadOrdersFlow: StateFlow<List<OrderWithItems>>
) {
    val context = LocalContext.current
    var fromDate by remember { mutableStateOf<Long?>(null) }
    var toDate by remember { mutableStateOf<Long?>(null) }
    var fromDateText by remember { mutableStateOf("من تاريخ") }
    var toDateText by remember { mutableStateOf("إلى تاريخ") }

    val historicalOrders by historicalOrdersFlow.collectAsState()
    val unreadOrders by unreadOrdersFlow.collectAsState()
    val isLoading by viewModel.isLoadingHistorical.collectAsState()

    fun openDatePicker(onDateSelected: (Long, String) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(context, { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            onDateSelected(selectedCalendar.timeInMillis, "$dayOfMonth/${month + 1}/$year")
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (unreadOrders.isNotEmpty()) {
            Text(
                text = "طلبات جديدة لم تتم قرائتها", color = Color(0xFFD32F2F),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                fontWeight = FontWeight.ExtraBold, fontSize = 16.sp
            )
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f, fill = false).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(unreadOrders, key = { it.order.order_id }) { orderWithItems ->
                    OrderExpandableCard(
                        data = orderWithItems, viewModel = viewModel, 
                        isEditable = false, showPdfExport = false, // تم إخفاء الـ PDF
                        onExpand = { viewModel.markOrderAsReadAndRemoveAlarm(orderWithItems.order.order_number.toLong()) }
                    )
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 2.dp, color = Color.LightGray)
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { openDatePicker { time, text -> fromDate = time; fromDateText = text } }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(fromDateText, fontSize = 12.sp)
                    }
                    OutlinedButton(onClick = { openDatePicker { time, text -> toDate = time; toDateText = text } }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(toDateText, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (fromDate != null && toDate != null) {
                            val endOfDay = toDate!! + 86399999L
                            viewModel.fetchOrdersByDate(status, fromDate!!, endOfDay)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D1B6D)),
                    enabled = fromDate != null && toDate != null && !isLoading
                ) {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    else Text("بحث في السجلات", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (historicalOrders.isEmpty() && !isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = if(fromDate == null) "الرجاء تحديد التاريخ والضغط على بحث" else "لا توجد طلبات في هذه الفترة", color = Color.Gray)
            }
        } else {
            OrdersList(orders = historicalOrders, viewModel = viewModel, isEditable = false, showPdfExport = false) // تم إخفاء الـ PDF
        }
    }
}

@Composable
fun OrdersList(orders: List<OrderWithItems>, viewModel: OrdersViewModel, isEditable: Boolean, showPdfExport: Boolean) {
    if (orders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("لا توجد طلبات هنا حالياً", fontSize = 18.sp, color = Color.Gray)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp)) {
            items(orders, key = { it.order.order_id }) { orderWithItems ->
                OrderExpandableCard(data = orderWithItems, viewModel = viewModel, isEditable = isEditable, showPdfExport = showPdfExport)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun OrderExpandableCard(
    data: OrderWithItems, 
    viewModel: OrdersViewModel, 
    isEditable: Boolean,
    showPdfExport: Boolean, // إضافة معامل للتحكم في ظهور زر الـ PDF
    onExpand: () -> Unit = {}
) {
    val order = data.order
    val items = data.items
    val context = LocalContext.current 

    var expanded by remember { mutableStateOf(false) }
    var deliveryFees by remember { mutableStateOf(order.delivery_fees.toString()) }

    val itemsPricingStates = remember {
        mutableStateMapOf<String, ItemAdminPricing>().apply {
            items.forEach { item ->
                put(item.item_id, ItemAdminPricing(item.item_id, item.purchase_price, item.selling_price, item.provider_name, item.invoice_number))
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { 
            expanded = !expanded 
            if (expanded) onExpand() 
        },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("المركبة: ${order.vehicle_name} - ${order.vehicle_model}", fontWeight = FontWeight.Bold, color = Color(0xFF0D1B6D), fontSize = 16.sp)
                    Text("الماركة: ${order.brand_name} | الصنع: ${order.manufacture}", color = Color.DarkGray, fontSize = 14.sp)
                    Text("الموقع: ${order.delivery_location}", color = Color.Gray, fontSize = 12.sp)
                    Text(
                        text = "الحالة: ${order.order_status}",
                        color = if (order.order_status == "canceled") Color.Red else Color(0xFF4CAF50),
                        fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Icon(imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = Color(0xFF0D1B6D))
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(color = Color.LightGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("القطع المطلوبة وتسعيرها:", fontWeight = FontWeight.Bold, color = Color(0xFF42A5F5))
                    Spacer(modifier = Modifier.height(8.dp))

                    items.forEach { item ->
                        val currentPricing = itemsPricingStates[item.item_id]!!
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).background(Color(0xFFF9F9F9), RoundedCornerShape(8.dp)).border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)).padding(12.dp)) {
                            Text("• ${item.part_name} (${item.quality_type}) | الكمية: ${item.quantity}", fontWeight = FontWeight.SemiBold)
                            if(item.description.isNotEmpty()) Text("الوصف: ${item.description}", fontSize = 12.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))

                            if (isEditable) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(value = if(currentPricing.purchasePrice == 0.0) "" else currentPricing.purchasePrice.toString(), onValueChange = { itemsPricingStates[item.item_id] = currentPricing.copy(purchasePrice = it.toDoubleOrNull() ?: 0.0) }, label = { Text("سعر الشراء", fontSize = 12.sp) }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                                    OutlinedTextField(value = if(currentPricing.sellingPrice == 0.0) "" else currentPricing.sellingPrice.toString(), onValueChange = { itemsPricingStates[item.item_id] = currentPricing.copy(sellingPrice = it.toDoubleOrNull() ?: 0.0) }, label = { Text("سعر البيع", fontSize = 12.sp) }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(value = currentPricing.providerName, onValueChange = { itemsPricingStates[item.item_id] = currentPricing.copy(providerName = it) }, label = { Text("اسم التاجر", fontSize = 12.sp) }, modifier = Modifier.weight(1f), singleLine = true)
                                    OutlinedTextField(value = currentPricing.invoiceNumber, onValueChange = { itemsPricingStates[item.item_id] = currentPricing.copy(invoiceNumber = it) }, label = { Text("رقم الفاتورة", fontSize = 12.sp) }, modifier = Modifier.weight(1f), singleLine = true)
                                }
                            } else {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("سعر البيع: ${currentPricing.sellingPrice}", fontSize = 13.sp, color = Color(0xFF0D1B6D))
                                    Text("سعر الشراء: ${currentPricing.purchasePrice}", fontSize = 13.sp, color = Color.DarkGray)
                                }
                                Text("التاجر: ${currentPricing.providerName.ifEmpty { "غير محدد" }} | الفاتورة: ${currentPricing.invoiceNumber.ifEmpty { "غير محدد" }}", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isEditable) {
                        OutlinedTextField(
                            value = deliveryFees,
                            onValueChange = { deliveryFees = it },
                            label = { Text("رسوم التوصيل الكلية") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF0D1B6D))
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            // زر الـ PDF يظهر في وضع التعديل (الطلبات المعلقة)
                            if (showPdfExport) {
                                OutlinedButton(
                                    onClick = { OrderPdfManager.generateOrderPdf(context, data) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0D1B6D)),
                                    modifier = Modifier.height(45.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("تصدير PDF", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                            
                            Row {
                                TextButton(onClick = { viewModel.rejectOrder(order.order_id) }) {
                                    Text("إلغاء", color = Color.Red, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Button(
                                    onClick = {
                                        viewModel.approveOrder(
                                            orderId = order.order_id,
                                            deliveryFees = deliveryFees.toDoubleOrNull() ?: 0.0,
                                            itemsPricing = itemsPricingStates.values.toList()
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                    modifier = Modifier.height(45.dp)
                                ) {
                                    Text("اعتماد التسعير", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("رسوم التوصيل: ${order.delivery_fees}", fontWeight = FontWeight.Bold, color = Color(0xFF0D1B6D))

                            // في غير وضع التعديل (مثل قيد الموافقة) يظهر إذا كان مسموحاً
                            if (showPdfExport) {
                                OutlinedButton(
                                    onClick = { OrderPdfManager.generateOrderPdf(context, data) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0D1B6D))
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("تصدير PDF", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
