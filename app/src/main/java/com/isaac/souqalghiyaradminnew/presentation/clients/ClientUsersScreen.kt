package com.isaac.souqalghiyaradminnew.presentation.clients

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientUsersScreen(
    viewModel: ClientUsersViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    // نستمع لقائمة العملاء (المفلترة) ونص البحث
    val clients by viewModel.clients.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("عملاء التطبيق", color = Color.White, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D1B6D))
                )
            },
            containerColor = Color(0xFFF5F5F5)
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                
                // شريط البحث
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text("ابحث برقم الهاتف...", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "بحث", tint = Color.Gray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), // فتح كيبورد الأرقام
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFF0D1B6D)
                    )
                )

                if (clients.isEmpty()) {
                    // رسالة في حال عدم وجود عملاء أو عدم وجود نتائج للبحث
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Text(
                            text = if (searchQuery.isNotEmpty()) "لا يوجد عميل بهذا الرقم" else "لا يوجد عملاء مسجلين حالياً",
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(clients, key = { it.user_id }) { client ->
                            val isActive = client.status == "active"
                            
                            ListItem(
                                colors = ListItemDefaults.colors(containerColor = Color.White),
                                headlineContent = { Text(client.display_name, fontWeight = FontWeight.Bold) },
                                supportingContent = {
                                    Column {
                                        Text("رقم الهاتف: ${client.phone_number}", color = Color.DarkGray)
                                        Text(
                                            text = if (isActive) "الحالة: نشط" else "الحالة: محظور",
                                            color = if (isActive) Color(0xFF4CAF50) else Color.Red,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                },
                                trailingContent = {
                                    IconButton(onClick = { viewModel.toggleClientStatus(client.user_id, client.status) }) {
                                        Icon(
                                            imageVector = if (isActive) Icons.Default.Block else Icons.Default.CheckCircle,
                                            contentDescription = if (isActive) "حظر" else "تفعيل",
                                            tint = if (isActive) Color.Red else Color(0xFF4CAF50)
                                        )
                                    }
                                }
                            )
                            HorizontalDivider(color = Color.LightGray)
                        }
                    }
                }
            }
        }
    }
}
