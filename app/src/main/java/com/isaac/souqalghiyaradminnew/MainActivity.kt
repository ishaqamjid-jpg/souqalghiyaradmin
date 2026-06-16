package com.isaac.souqalghiyaradminnew

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.isaac.souqalghiyaradminnew.presentation.login.LoginScreen
import com.isaac.souqalghiyaradminnew.presentation.dashboard.MainDashboardScreen
import com.isaac.souqalghiyaradminnew.presentation.users.UsersSettingsScreen
import com.isaac.souqalghiyaradminnew.presentation.orders.OrdersManagementScreen
import com.isaac.souqalghiyaradminnew.presentation.ads.AdsManagementScreen
import com.isaac.souqalghiyaradminnew.presentation.constants.ConstantsScreen
import com.isaac.souqalghiyaradminnew.presentation.reports.ReportsScreen
import com.isaac.souqalghiyaradminnew.presentation.clients.ClientUsersScreen
import com.isaac.souqalghiyaradminnew.ui.theme.SuqalghiyarAdminTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuqalghiyarAdminTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val sharedPref = getSharedPreferences("admin_prefs", Context.MODE_PRIVATE)

                    // قراءة حالة الدخول والبيانات المحفوظة
                    val isLoggedIn = sharedPref.getBoolean("is_logged_in", false)
                    val savedAdminName = sharedPref.getString("admin_name", "مدير النظام") ?: "مدير النظام"
                    val savedAdminId = sharedPref.getString("admin_id", "") ?: ""

                    // حفظ القيم في State للتعامل معها خلال الجلسة الحالية
                    var currentSessionId by remember { mutableStateOf(savedAdminId) }
                    var currentSessionName by remember { mutableStateOf(savedAdminName) }

                    val startDest = if (isLoggedIn) "dashboard" else "login"

                    NavHost(navController = navController, startDestination = startDest) {

                        // --- 1. شاشة تسجيل الدخول ---
                        composable("login") {
                            LoginScreen(
                                navigateToDashboard = { id, name, permissions ->
                                    // تحديث بيانات الجلسة الحالية عند الدخول بنجاح
                                    currentSessionId = id
                                    currentSessionName = name

                                    navController.navigate("dashboard") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // --- 2. الشاشة الرئيسية (الداش بورد) ---
                        composable("dashboard") {
                            MainDashboardScreen(
                                adminName = currentSessionName,
                                currentUserId = currentSessionId,
                                onNavigateToEmpUsers = { navController.navigate("users_emp") },
                                onNavigateToClientUsers = { navController.navigate("client_users") },
                                onNavigateToAds = { navController.navigate("ads") },
                                onNavigateToOrders = { navController.navigate("orders") },
                                onNavigateToConstants = { navController.navigate("constants") },
                                onNavigateToReports = { navController.navigate("reports") },
                                onLogoutClick = {
                                    // مسح البيانات محلياً والعودة لشاشة الدخول
                                    sharedPref.edit().clear().apply()
                                    currentSessionId = ""
                                    currentSessionName = ""
                                    navController.navigate("login") {
                                        popUpTo("dashboard") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // --- 3. شاشة إدارة الموظفين ---
                        composable("users_emp") {
                            UsersSettingsScreen(onBackClick = { navController.popBackStack() })
                        }

                        // --- 4. شاشة الطلبات ---
                        composable("orders") {
                            OrdersManagementScreen()
                        }

                        // --- 5. شاشة الإعلانات ---
                        composable("ads") {
                            AdsManagementScreen(onBackClick = { navController.popBackStack() })
                        }

                        // --- 6. شاشة الثوابت ---
                        composable("constants") {
                            ConstantsScreen(onBackClick = { navController.popBackStack() })
                        }

                        // --- 7. شاشة التقارير ---
                        composable("reports") {
                            ReportsScreen()
                        }

                        // --- 8. شاشة عملاء التطبيق ---
                        composable("client_users") {
                            ClientUsersScreen(onBackClick = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}
