package com.isaac.souqalghiyaradminnew.presentation.clients

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
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
import com.google.firebase.Timestamp
import com.isaac.souqalghiyaradminnew.domain.model.users

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientUsersScreen(
    viewModel: ClientUsersViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    // نستمع لقائمة العملاء (المفلترة) ونص البحث
    val clients by viewModel.clients.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    // متغير لإظهار أو إخفاء نافذة الإضافة
    var showAddDialog by remember { mutableStateOf(false) }

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
            // الزر العائم للإضافة
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = Color(0xFF0D1B6D),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "إضافة عميل")
                }
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFF0D1B6D)
                    )
                )

                if (clients.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (searchQuery.isNotEmpty()) "لا يوجد عميل بهذا الرقم" else "لا يوجد عملاء مسجلين حالياً",
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp) // لعدم تغطية الزر العائم لآخر بطاقة
                    ) {
                        items(clients, key = { it.user_id }) { client ->
                            ClientUserCard(client = client, viewModel = viewModel)
                        }
                    }
                }
            }
        }

        // إظهار نافذة الإضافة عند الضغط على الزر العائم
        if (showAddDialog) {
            AddClientDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { newClient ->
                    viewModel.addClient(newClient)
                    showAddDialog = false
                }
            )
        }
    }
}

// نافذة إضافة عميل جديد
@Composable
fun AddClientDialog(
    onDismiss: () -> Unit,
    onAdd: (users) -> Unit
) {
    var displayName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("active") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة عميل جديد", fontWeight = FontWeight.Bold, color = Color(0xFF0D1B6D)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("اسم العميل") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("رقم الهاتف") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = status,
                    onValueChange = { status = it },
                    label = { Text("الحالة (active / not_active)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (displayName.isNotBlank() && phoneNumber.isNotBlank()) {
                        val newClient = users(
                            display_name = displayName,
                            phone_number = phoneNumber,
                            status = status,
                            created_at = Timestamp.now()
                        )
                        onAdd(newClient)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D1B6D))
            ) { Text("إضافة", color = Color.White) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء", color = Color.Gray) }
        }
    )
}

@Composable
fun ClientUserCard(client: users, viewModel: ClientUsersViewModel) {
    var expanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) } // حالة نافذة تأكيد الحذف
    
    // متغيرات التعديل الخاصة بكل حقل
    var displayName by remember(client) { mutableStateOf(client.display_name) }
    var phoneNumber by remember(client) { mutableStateOf(client.phone_number) }
    var fcmToken by remember(client) { mutableStateOf(client.fcm_token) }
    var status by remember(client) { mutableStateOf(client.status) }
    var numberOfRejections by remember(client) { mutableStateOf(client.number_of_rejections.toString()) }

    val isActive = client.status == "active"

    // نافذة تأكيد الحذف
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("تأكيد الحذف") },
            text = { Text("هل أنت متأكد أنك تريد حذف العميل '${client.display_name}' بشكل نهائي؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteClient(client.user_id)
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("حذف", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("إلغاء", color = Color.Gray) }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { expanded = !expanded }, // عند الضغط تتوسع البطاقة
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // الرأس (يظهر دائماً)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(client.display_name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("رقم الهاتف: ${client.phone_number}", color = Color.DarkGray, fontSize = 14.sp)
                    Text(
                        text = if (isActive) "الحالة: نشط" else "الحالة: محظور",
                        color = if (isActive) Color(0xFF4CAF50) else Color.Red,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // زر الحذف
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف",
                            tint = Color.Red
                        )
                    }
                    
                    // زر الحظر السريع
                    IconButton(onClick = { viewModel.toggleClientStatus(client.user_id, client.status) }) {
                        Icon(
                            imageVector = if (isActive) Icons.Default.Block else Icons.Default.CheckCircle,
                            contentDescription = if (isActive) "حظر" else "تفعيل",
                            tint = if (isActive) Color.Red else Color(0xFF4CAF50)
                        )
                    }
                    
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "التفاصيل",
                        tint = Color.Gray
                    )
                }
            }

            // التفاصيل الموسعة (خانات التعديل)
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(color = Color.LightGray)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = { Text("اسم العميل") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("رقم الهاتف") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = status,
                        onValueChange = { status = it },
                        label = { Text("حالة الحساب (active / not_active)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = numberOfRejections,
                        onValueChange = { numberOfRejections = it },
                        label = { Text("عدد مرات الرفض") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = fcmToken,
                        onValueChange = { fcmToken = it },
                        label = { Text("FCM Token (لإرسال الإشعارات)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // زر الحفظ
                    Button(
                        onClick = {
                            val updatedClient = client.copy(
                                display_name = displayName,
                                phone_number = phoneNumber,
                                fcm_token = fcmToken,
                                status = status,
                                number_of_rejections = numberOfRejections.toDoubleOrNull() ?: 0.0
                            )
                            viewModel.updateClientDetails(updatedClient)
                            expanded = false // إغلاق البطاقة بعد الحفظ
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D1B6D))
                    ) {
                        Text("حفظ التعديلات", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
