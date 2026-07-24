package com.isaac.souqalghiyaradminnew.presentation.dashboard

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

// دالة مساعدة للتحقق من توفر الإنترنت
fun isInternetAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
    return when {
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
        else -> false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen(
    adminName: String,
    currentUserId: String,
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToEmpUsers: () -> Unit,
    onNavigateToClientUsers: () -> Unit,
    onNavigateToAds: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToConstants: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val pendingOrders by viewModel.pendingOrdersCount.collectAsState()
    val isAccountBanned by viewModel.isAccountBanned.collectAsState()
    val userPermissions by viewModel.userPermissions.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(currentUserId) {
        // التحقق من الإنترنت قبل بدء مراقبة الحساب
        if (currentUserId.isNotEmpty() && isInternetAvailable(context)) {
            viewModel.startMonitoringAccount(currentUserId)
        }
    }

    LaunchedEffect(isAccountBanned) {
        if (isAccountBanned) {
            onLogoutClick()
        }
    }

    // دالة لتنفيذ الإجراء فقط إذا كان هناك إنترنت
    fun performActionIfConnected(action: () -> Unit) {
        if (isInternetAvailable(context)) {
            action()
        } else {
            Toast.makeText(context, "لا يوجد اتصال بالإنترنت. يرجى التحقق من الشبكة.", Toast.LENGTH_SHORT).show()
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("لوحة تحكم سوق الغيار", fontWeight = FontWeight.ExtraBold) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color(0xFF1E1E1E),
                        titleContentColor = Color.White
                    ),
                    actions = {
                        IconButton(onClick = {
                            performActionIfConnected { viewModel.logout(onLogoutClick) }
                        }) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "تسجيل خروج", tint = Color.Red)
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFF121212))
                    .padding(16.dp)
            ) {
                Text(
                    text = "مرحباً بك، $adminName",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    item {
                        DashboardCard(
                            title = "الطلبات والتسعير",
                            icon = Icons.Default.ShoppingCart,
                            color = Color(0xFFE91E63),
                            badgeCount = pendingOrders,
                            onClick = { performActionIfConnected(onNavigateToOrders) }
                        )
                    }

                    item {
                        DashboardCard(
                            title = "التقارير والإحصائيات",
                            icon = Icons.Default.Assessment,
                            color = Color(0xFF4CAF50),
                            onClick = { performActionIfConnected(onNavigateToReports) }
                        )
                    }

                    if (userPermissions == "admin") {
                        item {
                            DashboardCard(
                                title = "إدارة الإعلانات",
                                icon = Icons.Default.Campaign,
                                color = Color(0xFFFF9800),
                                onClick = { performActionIfConnected(onNavigateToAds) }
                            )
                        }

                        item {
                            DashboardCard(
                                title = "عملاء التطبيق",
                                icon = Icons.Default.People,
                                color = Color(0xFF2196F3),
                                onClick = { performActionIfConnected(onNavigateToClientUsers) }
                            )
                        }

                        item {
                            DashboardCard(
                                title = "موظفي الإدارة",
                                icon = Icons.Default.AdminPanelSettings,
                                color = Color(0xFF9C27B0),
                                onClick = { performActionIfConnected(onNavigateToEmpUsers) }
                            )
                        }

                        item {
                            DashboardCard(
                                title = "إدارة الثوابت",
                                icon = Icons.Default.Category,
                                color = Color(0xFF00BCD4),
                                onClick = { performActionIfConnected(onNavigateToConstants) }
                            )
                        }

                        item {
                            DashboardCard(
                                title = "إعدادات النظام",
                                icon = Icons.Default.Settings,
                                color = Color(0xFF607D8B),
                                onClick = { performActionIfConnected(onNavigateToSettings) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    icon: ImageVector,
    color: Color,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(color.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(30.dp))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            if (badgeCount > 0) {
                Badge(
                    containerColor = Color.Red,
                    contentColor = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Text(text = badgeCount.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
