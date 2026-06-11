package com.isaac.souqalghiyaradminnew.presentation.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(viewModel: ReportsViewModel = hiltViewModel()) {
    val stats by viewModel.stats.collectAsState()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = { 
                TopAppBar(
                    title = { Text("تقارير النظام المالية", color = Color.White, fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D1B6D))
                ) 
            },
            containerColor = Color(0xFFF5F5F5)
        ) { padding ->
            Column(modifier = Modifier.padding(padding).padding(16.dp)) {
                
                Text("إحصائيات الطلبات المكتملة", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("عدد الطلبات المكتملة", stats.totalCompletedOrders.toString(), Color(0xFF2196F3), Modifier.weight(1f))
                    StatCard("إجمالي الإيرادات", "${stats.totalRevenue} ر.ي", Color(0xFFFF9800), Modifier.weight(1f))
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("إجمالي التكاليف (المشتريات)", "${stats.totalCosts} ر.ي", Color(0xFFF44336), Modifier.weight(1f))
                    StatCard("صافي الأرباح", "${stats.netProfit} ر.ي", Color(0xFF4CAF50), Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(120.dp), 
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}
