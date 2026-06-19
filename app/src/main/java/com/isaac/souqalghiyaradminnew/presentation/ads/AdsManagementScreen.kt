package com.isaac.souqalghiyaradminnew.presentation.ads

import android.app.DatePickerDialog
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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.Timestamp
import com.isaac.souqalghiyaradminnew.domain.model.Ad
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdsManagementScreen(
    viewModel: AdsViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val ads by viewModel.ads.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var adToEdit by remember { mutableStateOf<Ad?>(null) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("إدارة الإعلانات", color = Color.White, fontWeight = FontWeight.Bold) },
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
                    onClick = { adToEdit = null; showDialog = true },
                    containerColor = Color(0xFF4CAF50)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "إضافة", tint = Color.White)
                }
            },
            containerColor = Color(0xFFF5F5F5)
        ) { padding ->
            if (ads.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("لا توجد إعلانات حالياً", fontSize = 18.sp, color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(ads, key = { it.ad_id }) { ad ->
                        AdItemCard(
                            ad = ad,
                            onEdit = { adToEdit = ad; showDialog = true },
                            onToggleStatus = { viewModel.toggleAdStatus(ad) },
                            onDelete = { viewModel.deleteAd(ad.ad_id) }
                        )
                    }
                }
            }

            if (showDialog) {
                AdEditorDialog(
                    initialAd = adToEdit,
                    onDismiss = { showDialog = false; adToEdit = null },
                    onConfirm = { editedAd ->
                        viewModel.saveAd(editedAd)
                        showDialog = false
                        adToEdit = null
                    }
                )
            }
        }
    }
}

@Composable
fun AdItemCard(
    ad: Ad,
    onEdit: () -> Unit,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val startDateFormatted = ad.start_date?.toDate()?.let { dateFormat.format(it) } ?: "غير محدد"
    val endDateFormatted = ad.end_date?.toDate()?.let { dateFormat.format(it) } ?: "غير محدد"

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onEdit() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = ad.title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF0D1B6D))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "الرابط: ${ad.target_url ?: "لا يوجد"}", fontSize = 12.sp, color = Color.Gray)
                    Text(text = "تاريخ البدء: $startDateFormatted", fontSize = 12.sp, color = Color(0xFF4CAF50))
                    Text(text = "تاريخ الانتهاء: $endDateFormatted", fontSize = 12.sp, color = Color.Red)
                    Text(text = "الأولوية: ${ad.priority}", fontSize = 12.sp, color = Color.Gray)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Red)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("نشط:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Switch(
                        checked = ad.is_active,
                        onCheckedChange = { onToggleStatus() },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF4CAF50))
                    )
                }
                Text(
                    text = if (ad.is_active) "يظهر للعملاء" else "موقوف",
                    color = if (ad.is_active) Color(0xFF4CAF50) else Color.Red,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun AdEditorDialog(
    initialAd: Ad?,
    onDismiss: () -> Unit,
    onConfirm: (Ad) -> Unit
) {
    var title by remember { mutableStateOf(initialAd?.title ?: "") }
    var imageUrl by remember { mutableStateOf(initialAd?.image_url ?: "") }
    var clickActionType by remember { mutableStateOf(initialAd?.click_action_type ?: "") }
    var targetUrl by remember { mutableStateOf(initialAd?.target_url ?: "") }
    var priority by remember { mutableStateOf(initialAd?.priority?.toString() ?: "0") }
    var isActive by remember { mutableStateOf(initialAd?.is_active ?: true) }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    val context = LocalContext.current

    var startDateStr by remember {
        mutableStateOf(initialAd?.start_date?.toDate()?.let { dateFormat.format(it) } ?: "")
    }
    var endDateStr by remember {
        mutableStateOf(initialAd?.end_date?.toDate()?.let { dateFormat.format(it) } ?: "")
    }

    // دالة مساعدة لفتح DatePickerDialog
    val showDatePicker = { currentDate: String, onDateSelected: (String) -> Unit ->
        val calendar = Calendar.getInstance()
        if (currentDate.isNotBlank()) {
            try {
                dateFormat.parse(currentDate)?.let { calendar.time = it }
            } catch (e: Exception) { /* تجاهل الخطأ واستخدم تاريخ اليوم */ }
        }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
                onDateSelected(dateFormat.format(selectedDate.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialAd == null) "إضافة إعلان" else "تعديل الإعلان") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان الإعلان") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("رابط الصورة (URL)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    modifier = Modifier.fillMaxWidth()
                )

                // حقل تاريخ البدء مع DatePicker
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = startDateStr,
                        onValueChange = { },
                        label = { Text("تاريخ البدء") },
                        singleLine = true,
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = "اختر التاريخ") }
                    )
                    // Spacer شفاف لالتقاط النقرات وفتح التقويم
                    Spacer(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showDatePicker(startDateStr) { startDateStr = it } }
                    )
                }

                // حقل تاريخ الانتهاء مع DatePicker
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = endDateStr,
                        onValueChange = { },
                        label = { Text("تاريخ الانتهاء") },
                        singleLine = true,
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = "اختر التاريخ") }
                    )
                    Spacer(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showDatePicker(endDateStr) { endDateStr = it } }
                    )
                }

                OutlinedTextField(
                    value = clickActionType,
                    onValueChange = { clickActionType = it },
                    label = { Text("نوع الإجراء (مثال: open_url)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = targetUrl,
                    onValueChange = { targetUrl = it },
                    label = { Text("الرابط المستهدف") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = priority,
                    onValueChange = { priority = it },
                    label = { Text("الأولوية (رقم)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("الإعلان نشط:")
                    Spacer(Modifier.width(8.dp))
                    Switch(checked = isActive, onCheckedChange = { isActive = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    fun parseToMidnightTimestamp(dateStr: String): Timestamp? {
                        return try {
                            if (dateStr.isNotBlank()) {
                                val parsedDate = dateFormat.parse(dateStr)
                                val calendar = Calendar.getInstance().apply {
                                    if (parsedDate != null) time = parsedDate
                                    set(Calendar.HOUR_OF_DAY, 0)
                                    set(Calendar.MINUTE, 0)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }
                                Timestamp(calendar.time)
                            } else null
                        } catch (e: Exception) { null }
                    }

                    val startTimestamp = parseToMidnightTimestamp(startDateStr)
                    val endTimestamp = parseToMidnightTimestamp(endDateStr)

                    val newAd = Ad(
                        ad_id = initialAd?.ad_id ?: "",
                        title = title,
                        image_url = imageUrl,
                        click_action_type = clickActionType,
                        target_url = targetUrl.ifBlank { null },
                        start_date = startTimestamp,
                        end_date = endTimestamp,
                        priority = priority.toIntOrNull() ?: 0,
                        is_active = isActive,
                        created_at = initialAd?.created_at
                    )
                    onConfirm(newAd)
                }
            ) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}