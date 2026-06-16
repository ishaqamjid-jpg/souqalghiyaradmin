package com.isaac.souqalghiyaradminnew.presentation.users

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.isaac.souqalghiyaradminnew.domain.model.UserEmp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersSettingsScreen(
    viewModel: UsersViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val users by viewModel.users.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var userToEdit by remember { mutableStateOf<UserEmp?>(null) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("إدارة موظفي النظام", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D1B6D))
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { userToEdit = null; showDialog = true },
                    containerColor = Color(0xFF4CAF50)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "إضافة", tint = Color.White)
                }
            }
        ) { padding ->
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(users) { user ->
                    ListItem(
                        headlineContent = { Text(user.display_name, fontWeight = FontWeight.Bold) },
                        supportingContent = { 
                            Column {
                                Text("رقم الهاتف: ${user.phone_number}")
                                Text("كلمة المرور: ${user.password}") // يفضل إظهارها للإدارة أو إخفائها حسب الرغبة
                                Text(
                                    text = if (user.status == "active") "الحالة: نشط" else "الحالة: موقوف",
                                    color = if (user.status == "active") Color(0xFF4CAF50) else Color.Red
                                )
                                Text("الصلاحية: ${if(user.user_permissions == "admin") "مدير" else "موظف"}")
                            }
                        },
                        trailingContent = {
                            Row {
                                IconButton(onClick = { userToEdit = user; showDialog = true }) {
                                    Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = Color(0xFF2196F3))
                                }
                                IconButton(onClick = { viewModel.deleteUser(user.user_id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Red)
                                }
                            }
                        }
                    )
                    HorizontalDivider()
                }
            }

            if (showDialog) {
                UserEditorDialog(
                    user = userToEdit,
                    onDismiss = { showDialog = false },
                    // تم إضافة استلام password هنا وتمريرها للدالة
                    onSave = { id, displayName, phoneNumber, password, permissions, status ->
                        viewModel.saveUser(id, displayName, phoneNumber, password, permissions, status)
                        showDialog = false
                    }
                )
            }
        }
    }
}
