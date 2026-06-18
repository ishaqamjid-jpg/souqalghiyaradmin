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

    // States للبحث
    val merchantName by viewModel.merchantName.collectAsState()
    val partName by viewModel.partName.collectAsState()
    val orderNumber by viewModel.orderNumber.collectAsState()
    val vehicleModel by viewModel.vehicleModel.collectAsState()
    val orderStatus by viewModel.orderStatus.collectAsState()
    val fromDate by viewModel.fromDate.collectAsState()
    val toDate by viewModel.toDate.collectAsState()
    val isDateEnabled by viewModel.isDateFilterEnabled.collectAsState()

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
                
                // 1. الإحصائيات العلوية
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard("المكتملة", stats.totalCompletedOrders.toString(), Color(0xFF2196F3), Modifier.weight(1f))
                    StatCard("الإيرادات", "${stats.totalRevenue}", Color(0xFFFF9800), Modifier.weight(1f))
                    StatCard("التكاليف", "${stats.totalCosts}", Color(0xFFF44336), Modifier.weight(1f))
                    StatCard("الأرباح", "${stats.netProfit}", Color(0xFF4CAF50), Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                // 2. خانات البحث
                Text("فلاتر البحث:", fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(value = orderNumber, onValueChange = { viewModel.orderNumber.value = it }, label = { Text("رقم الطلب") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = orderStatus, onValueChange = { viewModel.orderStatus.value = it }, label = { Text("حالة الطلب") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(value = merchantName, onValueChange = { viewModel.merchantName.value = it }, label = { Text("اسم التاجر") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = partName, onValueChange = { viewModel.partName.value = it }, label = { Text("اسم القطعة") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                
                Row(vertical
