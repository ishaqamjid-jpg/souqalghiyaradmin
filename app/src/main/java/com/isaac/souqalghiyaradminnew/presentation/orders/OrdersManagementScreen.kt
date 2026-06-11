package com.isaac.souqalghiyaradminnew.presentation.orders

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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.isaac.souqalghiyaradminnew.domain.model.OrderWithItems

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersManagementScreen(
    viewModel: OrdersViewModel = hiltViewModel()
) {
    val orders by viewModel.pendingOrders.collectAsState()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("إدارة الطلبات المعلقة", color = Color.White, fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D1B6D))
                )
            },
            containerColor = Color(0xFFF5F5F5)
        ) { padding ->
            if (orders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("لا توجد طلبات معلقة حالياً", fontSize = 18.sp, color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(orders, key = { it.order.order_id }) { orderWithItems ->
                        OrderExpandableCard(data = orderWithItems, viewModel = viewModel)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun OrderExpandableCard(data: OrderWithItems, viewModel: OrdersViewModel) {
    val order = data.order
    val items = data.items

    var expanded by remember { mutableStateOf(false) }
    var deliveryFees by remember { mutableStateOf("") }
    
    // حالة محلية لحفظ التعديلات لكل قطعة قبل إرسالها
    val itemsPricingStates = remember {
        mutableStateMapOf<String, ItemAdminPricing>().apply {
            items.forEach { item ->
                put(item.item_id, ItemAdminPricing(item.item_id, 0.0, 0.0, "", ""))
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // رأس البطاقة
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("المركبة: ${order.vehicle_name} - ${order.vehicle_model}", fontWeight = FontWeight.Bold, color = Color(0xFF0D1B6D), fontSize = 16.sp)
                    Text("الماركة: ${order.brand_name} | الصنع: ${order.manufacture}", color = Color.DarkGray, fontSize = 14.sp)
                    Text("الموقع: ${order.delivery_location}", color = Color.Gray, fontSize = 12.sp)
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = Color(0xFF0D1B6D)
                )
            }

            // التفاصيل عند التوسع
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(color = Color.LightGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text("القطع المطلوبة وتسعيرها:", fontWeight = FontWeight.Bold, color = Color(0xFF42A5F5))
                    Spacer(modifier = Modifier.height(8.dp))

                    // عرض القطع وخانات التسعير لكل قطعة
                    items.forEach { item ->
                        val currentPricing = itemsPricingStates[item.item_id]!!

                        Column(modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .background(Color(0xFFF9F9F9), RoundedCornerShape(8.dp))
                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                        ) {
                            Text("• ${item.part_name} (${item.quality_type}) | الكمية: ${item.quantity}", fontWeight = FontWeight.SemiBold)
                            if(item.description.isNotEmpty()) Text("الوصف: ${item.description}", fontSize = 12.sp, color = Color.Gray)
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = if(currentPricing.purchasePrice == 0.0) "" else currentPricing.purchasePrice.toString(),
                                    onValueChange = { 
                                        itemsPricingStates[item.item_id] = currentPricing.copy(purchasePrice = it.toDoubleOrNull() ?: 0.0) 
                                    },
                                    label = { Text("سعر الشراء", fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = if(currentPricing.sellingPrice == 0.0) "" else currentPricing.sellingPrice.toString(),
                                    onValueChange = { 
                                        itemsPricingStates[item.item_id] = currentPricing.copy(sellingPrice = it.toDoubleOrNull() ?: 0.0) 
                                    },
                                    label = { Text("سعر البيع", fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = currentPricing.providerName,
                                    onValueChange = { 
                                        itemsPricingStates[item.item_id] = currentPricing.copy(providerName = it) 
                                    },
                                    label = { Text("اسم التاجر", fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = currentPricing.invoiceNumber,
                                    onValueChange = { 
                                        itemsPricingStates[item.item_id] = currentPricing.copy(invoiceNumber = it) 
                                    },
                                    label = { Text("رقم الفاتورة", fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // رسوم التوصيل للطلب ككل
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

                    // أزرار التحكم
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { viewModel.rejectOrder(order.order_id) }) {
                            Text("رفض وإلغاء", color = Color.Red, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { 
                                viewModel.approveOrder(
                                    orderId = order.order_id,
                                    deliveryFees = deliveryFees.toDoubleOrNull() ?: 0.0,
                                    itemsPricing = itemsPricingStates.values.toList()
                                ) 
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Text("اعتماد وإرسال للعميل", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
