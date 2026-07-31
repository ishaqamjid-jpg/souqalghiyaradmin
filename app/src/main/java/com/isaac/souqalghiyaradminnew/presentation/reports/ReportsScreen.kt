package com.isaac.souqalghiyaradminnew.presentation.reports

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel = hiltViewModel(),
    isAdmin: Boolean = true
) {
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

    // --- التحكم بالتابات ---
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("الإحصائيات", "التقارير وسجل الطلبات")

    val statusOptions = listOf(
        "" to "الكل (فارغ)",
        "completed" to "مكتملة",
        "canceled" to "مرفوضة",
        "pending" to "معلقة",
        "going" to "جاري التوصيل",
        "waiting for approvel" to "انتظار الموافقة"
    )

    val selectedStatusText = statusOptions.find { it.first == orderStatus }?.second ?: "الكل (فارغ)"

    fun openDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(context, { _, year, month, dayOfMonth ->
            val formattedDate = String.format(Locale.ENGLISH, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
            onDateSelected(formattedDate)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            if (isAdmin) "التقارير والإحصائيات" else "التقارير وسجل الطلبات",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D1B6D))
                )
            },
            containerColor = Color(0xFFF5F5F5)
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {

                // ==================== شريط التابات (يظهر للأدمن فقط) ====================
                if (isAdmin) {
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = Color.White,
                        contentColor = Color(0xFF0D1B6D)
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp) }
                            )
                        }
                    }
                }

                // ==================== محتوى التابات ====================
                Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {

                    if (isAdmin && selectedTabIndex == 0) {
                        // ----------------- التاب الأول: الإحصائيات (للأدمن فقط) -----------------
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                    Switch(checked = isDateEnabled, onCheckedChange = { viewModel.isDateFilterEnabled.value = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF0D1B6D), checkedTrackColor = Color(0xFF0D1B6D).copy(alpha = 0.5f)))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("تفعيل فلتر التاريخ للتحليل", fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                }

                                AnimatedVisibility(visible = isDateEnabled) {
                                    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(onClick = { openDatePicker { viewModel.fromDate.value = it } }, modifier = Modifier.weight(1f)) {
                                            Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFF0D1B6D))
                                            Spacer(Modifier.width(4.dp))
                                            Text(if(fromDate.isEmpty()) "من تاريخ" else fromDate, fontSize = 12.sp, color = Color.DarkGray)
                                        }
                                        OutlinedButton(onClick = { openDatePicker { viewModel.toDate.value = it } }, modifier = Modifier.weight(1f)) {
                                            Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFF0D1B6D))
                                            Spacer(Modifier.width(4.dp))
                                            Text(if(toDate.isEmpty()) "إلى تاريخ" else toDate, fontSize = 12.sp, color = Color.DarkGray)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = { viewModel.searchOrders() },
                                    modifier = Modifier.fillMaxWidth().height(45.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D1B6D))
                                ) {
                                    Icon(Icons.Default.Search, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("تحليل وعرض الإحصائيات", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text("النتائج الإحصائية:", fontWeight = FontWeight.Bold, color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(12.dp))

                        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                StatCard("المكتملة", stats.totalCompletedOrders.toString(), Color(0xFF4CAF50), Modifier.weight(1f))
                                StatCard("المرفوضة", stats.totalCanceledOrders.toString(), Color(0xFFE53935), Modifier.weight(1f))
                                StatCard("المواصلات", "${stats.totalTransportation}", Color(0xFF00ACC1), Modifier.weight(1f))
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                StatCard("التكاليف", "${stats.totalCosts}", Color(0xFFF57C00), Modifier.weight(1f))
                                StatCard("الإيرادات", "${stats.totalRevenue}", Color(0xFF8E24AA), Modifier.weight(1f))
                                StatCard("الربح", "${stats.netProfit}", Color(0xFF2E7D32), Modifier.weight(1f))
                            }
                        }

                    } else {
                        // ----------------- التاب الثاني / الشاشة للمستخدم العادي: التقارير وسجل الطلبات -----------------

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("فلاتر البحث الدقيق:", fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(value = orderNumber, onValueChange = { viewModel.orderNumber.value = it }, label = { Text("رقم الطلب") }, modifier = Modifier.weight(1f), singleLine = true)

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

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                                    OutlinedTextField(value = merchantName, onValueChange = { viewModel.merchantName.value = it }, label = { Text("اسم التاجر") }, modifier = Modifier.weight(1f), singleLine = true)
                                    OutlinedTextField(value = partName, onValueChange = { viewModel.partName.value = it }, label = { Text("اسم القطعة") }, modifier = Modifier.weight(1f), singleLine = true)
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = { viewModel.searchOrders() },
                                    modifier = Modifier.fillMaxWidth().height(45.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D1B6D))
                                ) {
                                    Icon(Icons.Default.Search, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("بحث في السجل", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // زر تصدير PDF
                        Button(
                            onClick = { ReportsPdfManager.generateFilteredReportPdf(context, filteredOrders) },
                            modifier = Modifier.fillMaxWidth().height(45.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f)),
                            enabled = hasSearched && filteredOrders.isNotEmpty()
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تصدير النتائج إلى PDF", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // عرض النتائج في الأسفل
                        if (!hasSearched) {
                            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text("قم بضبط الفلاتر واضغط على بحث لعرض السجل", color = Color.Gray, fontSize = 14.sp)
                            }
                        } else if (filteredOrders.isEmpty()) {
                            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text("لا توجد طلبات تطابق معايير البحث", color = Color.Gray, fontSize = 14.sp)
                            }
                        } else {
                            Text("سجل الطلبات المطابقة (${filteredOrders.size}):", fontWeight = FontWeight.Bold, color = Color.DarkGray)
                            Spacer(modifier = Modifier.height(8.dp))

                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filteredOrders) { orderData ->
                                    FullOrderDetailsCard(orderData = orderData, isAdmin = isAdmin)
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
fun FullOrderDetailsCard(
    orderData: com.isaac.souqalghiyaradminnew.domain.model.OrderWithItems,
    isAdmin: Boolean = true
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.ENGLISH)
    val orderDate = when (val ts = orderData.order.created_at) {
        is com.google.firebase.Timestamp -> dateFormat.format(ts.toDate())
        else -> "غير محدد"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "رقم الطلب: ${orderData.order.order_number}", fontWeight = FontWeight.Bold, color = Color(0xFF0D1B6D))
                Text(
                    text = "الحالة: ${orderData.order.order_status}",
                    fontWeight = FontWeight.Bold,
                    color = when(orderData.order.order_status.lowercase()) {
                        "completed" -> Color(0xFF4CAF50)
                        "canceled" -> Color.Red
                        "going" -> Color(0xFF03A9F4)
                        else -> Color.DarkGray
                    }
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "التاريخ: $orderDate", fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))

            val fullVehicleName = "${orderData.order.brand_name} ${orderData.order.vehicle_name} ${orderData.order.vehicle_model}".trim()
            Text(text = "المركبة: $fullVehicleName - ${orderData.order.manufacture}", fontSize = 14.sp)

            Text(text = "رقم الشاصي: ${orderData.order.vin_number.ifEmpty { "غير متوفر" }}", fontSize = 14.sp)

            Text(text = "رسوم التوصيل: ${orderData.order.delivery_fees} ر.ي", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)

            if (orderData.order.order_status.equals("completed", ignoreCase = true) && orderData.order.approval_notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "ملاحظات الموافقة: ${orderData.order.approval_notes}", fontSize = 14.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Medium)
            } else if (orderData.order.order_status.equals("canceled", ignoreCase = true) && orderData.order.disapproval_notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "سبب الرفض: ${orderData.order.disapproval_notes}", fontSize = 14.sp, color = Color.Red, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.LightGray)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "القطع المطلوبة (${orderData.items.size}):", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(4.dp))

            orderData.items.forEach { item ->
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).background(Color(0xFFF9F9F9), RoundedCornerShape(8.dp)).padding(8.dp)) {
                    Text(text = "اسم القطعة: ${item.part_name}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "الكمية: ${item.quantity}", fontSize = 12.sp)
                        Text(text = "التاجر: ${item.provider_name.ifEmpty { "غير محدد" }}", fontSize = 12.sp)
                        Text(text = "الفاتورة: ${item.invoice_number.ifEmpty { "-" }}", fontSize = 12.sp)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        if (isAdmin) {
                            Text(text = "سعر الشراء: ${item.purchase_price}", fontSize = 12.sp, color = Color.Red)
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                        Text(text = "سعر البيع: ${item.selling_price}", fontSize = 12.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(75.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(
            modifier = Modifier.padding(6.dp).fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}