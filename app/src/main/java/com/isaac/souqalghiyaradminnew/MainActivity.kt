package com.isaac.souqalghiyaradminnew

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.messaging.FirebaseMessaging
import com.isaac.souqalghiyaradminnew.presentation.login.LoginScreen
import com.isaac.souqalghiyaradminnew.presentation.dashboard.MainDashboardScreen
import com.isaac.souqalghiyaradminnew.presentation.users.UsersSettingsScreen
import com.isaac.souqalghiyaradminnew.presentation.orders.OrdersManagementScreen
import com.isaac.souqalghiyaradminnew.presentation.ads.AdsManagementScreen
import com.isaac.souqalghiyaradminnew.presentation.constants.ConstantsScreen
import com.isaac.souqalghiyaradminnew.presentation.reports.ReportsScreen
import com.isaac.souqalghiyaradminnew.presentation.clients.ClientUsersScreen
import com.isaac.souqalghiyaradminnew.presentation.settings.SettingsScreen
import com.isaac.souqalghiyaradminnew.presentation.settings.BackupScreen
import com.isaac.souqalghiyaradminnew.presentation.settings.AdvancedOrdersScreen
import com.isaac.souqalghiyaradminnew.ui.theme.SuqalghiyarAdminTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean -> }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        askNotificationPermission()

        setContent {
            SuqalghiyarAdminTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val sharedPref = getSharedPreferences("admin_prefs", Context.MODE_PRIVATE)

                    val isLoggedIn = sharedPref.getBoolean("is_logged_in", false)
                    val savedAdminName = sharedPref.getString("admin_name", "مدير النظام") ?: "مدير النظام"
                    val savedAdminId = sharedPref.getString("admin_id", "") ?: ""
                    val savedAdminPermissions = sharedPref.getString("admin_permissions", "employee") ?: "employee"

                    if (isLoggedIn) {
                        FirebaseMessaging.getInstance().subscribeToTopic("admin_notifications")
                    }

                    var currentSessionId by remember { mutableStateOf(savedAdminId) }
                    var currentSessionName by remember { mutableStateOf(savedAdminName) }
                    var currentSessionPermissions by remember { mutableStateOf(savedAdminPermissions) }

                    val startDest = if (isLoggedIn) "dashboard" else "login"

                    NavHost(navController = navController, startDestination = startDest) {

                        composable("login") {
                            LoginScreen(
                                navigateToDashboard = { id, name, permissions ->
                                    currentSessionId = id
                                    currentSessionName = name
                                    currentSessionPermissions = permissions

                                    FirebaseMessaging.getInstance().subscribeToTopic("admin_notifications")

                                    navController.navigate("dashboard") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("dashboard") {
                            MainDashboardScreen(
                                adminName = currentSessionName,
                                currentUserId = currentSessionId,
                                onNavigateToEmpUsers = { navController.navigate("UserEmp") },
                                onNavigateToClientUsers = { navController.navigate("client_users") },
                                onNavigateToAds = { navController.navigate("ads") },
                                onNavigateToOrders = { navController.navigate("orders") },
                                onNavigateToConstants = { navController.navigate("constants") },
                                onNavigateToReports = { navController.navigate("reports") },
                                onNavigateToSettings = { navController.navigate("settings") },
                                onLogoutClick = {
                                    FirebaseMessaging.getInstance().unsubscribeFromTopic("admin_notifications")
                                    
                                    sharedPref.edit().clear().apply()
                                    currentSessionId = ""
                                    currentSessionName = ""
                                    currentSessionPermissions = ""
                                    navController.navigate("login") {
                                        popUpTo("dashboard") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("UserEmp") {
                            UsersSettingsScreen(onBackClick = { navController.popBackStack() })
                        }

                        composable("orders") {
                            OrdersManagementScreen()
                        }

                        composable("ads") {
                            AdsManagementScreen(onBackClick = { navController.popBackStack() })
                        }

                        composable("constants") {
                            ConstantsScreen(onBackClick = { navController.popBackStack() })
                        }

                        composable("reports") {
                            ReportsScreen(
                                isAdmin = currentSessionPermissions == "admin"
                            )
                        }

                        composable("client_users") {
                            ClientUsersScreen(onBackClick = { navController.popBackStack() })
                        }

                        composable("settings") {
                            SettingsScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToBackup = { navController.navigate("backup") },
                                onNavigateToAdvancedOrders = { navController.navigate("advanced_orders") }
                            )
                        }

                        composable("backup") {
                            BackupScreen(onNavigateBack = { navController.popBackStack() })
                        }

                        composable("advanced_orders") {
                            AdvancedOrdersScreen(onNavigateBack = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}
