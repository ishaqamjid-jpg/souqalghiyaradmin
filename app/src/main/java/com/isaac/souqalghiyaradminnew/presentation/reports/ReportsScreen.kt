package com.isaac.souqalghiyaradminnew.presentation.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(viewModel: ReportsViewModel = hiltViewModel()) {
    val stats by viewModel.stats.collectAsState()
    val filteredOrders by viewModel.filteredOrders.collectAsState()
    val hasSearched by viewModel.hasSearched.collectAsState()
    val context = LocalContext.current

    val merchantName by viewModel.merchantName.collectAsState()
    val partName by viewModel.partName.collectAsState()
    val orderNumber by viewModel.orderNumber.collectAsState()
    val vehicleModel by viewModel.vehicleModel.collectAsState()
    val orderStatus by viewModel.orderStatus.collectAsState()
    val fromDate by viewModel.fromDate.collectAsState()
    val toDate by viewModel.toDate.collectAsState()
    val isDateEnabled by viewModel.isDateFilterEnabled.collectAsState()

    var expandedStatus by remember { mutableStateOf(false) }

    // قائمة الحالات المنسدلة
    val statusOptions = listOf(
        "" to "الكل (فارغ)",
        "completed" to "مكتملة (completed)",
        "canceled" to "مرفوض (canceled)",
        "pending" to "معلقة (pending)",
        "waiting for approvel" to "انتظار الموافقة"
    )

    // الحصول على النص المعروض بناءً على القيمة المختارة
    val selectedStatusText = statusOptions.find { it.first == orderStatus }?.second ?: "الكل (فارغ)"

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = { 
                TopAppBar(
                    title = { Text("التقارير المتقدمة", color = Color.White, fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D1B6D))
                ) 
            },
            containerColor = Color(0xFFF5F5F5)
        ) { padding ->
            Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard("المكتملة", stats.totalCompletedOrders.toString(), Color(0xFF2196F3), Modifier.weight(1f))
                    StatCard("الإيرادات", "${stats.totalRevenue}", Color(0xFFFF9800), Modifier.weight(1f))
                    StatCard("التكاليف", "${stats.totalCosts}", Color(0xFFF44336), Modifier.weight(1f))
                    StatCard("الأرباح", "${stats.netProfit}", Color(0xFF4CAF50), Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                Text("فلاتر البحث:", fontWeight = FontWeight.Bold)
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(value = orderNumber, onValueChange = { viewModel.orderNumber.value = it }, label = { Text("رقم الطلب") }, modifier = Modifier.weight(1f), singleLine = true)
                    
                    // القائمة المنسدلة لحالة الطلب
                    ExposedDropdownMenuBox(
                        expanded = expandedStatus,
                        onExpandedChange = { expandedStatus = !expandedStatus },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedStatusText,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("حالة الطلب") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            singleLine = true
                        )
                        ExposedDropdownMenu(
                            expanded = expandedStatus,
                            onDismissRequest = { expandedStatus = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            statusOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.second, color = Color.Black) },
                                    onClick = {
                                        viewModel.orderStatus.value = option.first
                                        expandedStatus = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(value = merchantName, onValueChange = { viewModel.merchantName.value = it }, label = { Text("اسم التاجر") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = partName, onValueChange = { viewModel.partName.value = it }, label = { Text("اسم القطعة") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Switch(checked = isDateEnabled, onCheckedChange = { viewModel.isDateFilterEnabled.value = it })
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(value = fromDate, onValueChange = { viewModel.fromDate.value = it }, label = { Text("من (yyyy-MM-dd)") }, modifier = Modifier.weight(1f), enabled = isDateEnabled)
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(value = toDate, onValueChange = { viewModel.toDate.value = it }, label = { Text("إلى (yyyy-MM-dd)") }, modifier = Modifier.weight(1f), enabled = isDateEnabled)
                }

                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Button(
                        onClick = { viewModel.searchOrders() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D1B6D))
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("بحث / عرض الكل")
                    }

                    Button(
                        onClick = { ReportsPdfManager.generateFilteredReportPdf(context, filteredOrders) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f)),
                        enabled = hasSearched && filteredOrders.isNotEmpty()
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تصدير PDF")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (!hasSearched) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("قم بضبط الفلاتر (أو اتركها فارغة لعرض الكل) واضغط على زر البحث", color = Color.Gray, fontSize = 14.sp)
                    }
                } else if (filteredOrders.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("لا توجد طلبات تطابق معايير البحث", color = Color.Gray, fontSize = 14.sp)
                    }
                } else {
                    Text("نتائج البحث (${filteredOrders.size} طلب):", fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth().background(Color(0xFF0D1B6D)).padding(8.dp)) {
                        Text("الطلب", color = Color.White, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                        Text("القطعة", color = Color.White, modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold)
                        Text("التاجر", color = Color.White, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                        Text("السعر", color = Color.White, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    }

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(filteredOrders) { orderData ->
                            orderData.items.forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, Color.LightGray)
                                        .background(Color.White)
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(orderData.order.order_id.take(5) + "..", modifier = Modifier.weight(1f), fontSize = 12.sp)
                                    Text(item.part_name, modifier = Modifier.weight(1.5f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(item.provider_name.ifEmpty { "-" }, modifier = Modifier.weight(1f), fontSize = 12.sp)
                                    Text("${item.selling_price}", modifier = Modifier.weight(1f), fontSize = 12.sp, color = Color(0xFF4CAF50))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(80.dp), 
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp).fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
