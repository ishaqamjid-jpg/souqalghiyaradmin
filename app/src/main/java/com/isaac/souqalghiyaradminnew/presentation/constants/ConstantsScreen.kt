package com.isaac.souqalghiyaradminnew.presentation.constants

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.isaac.souqalghiyaradminnew.domain.model.Brand
import com.isaac.souqalghiyaradminnew.domain.model.Location
import com.isaac.souqalghiyaradminnew.domain.model.QualityType
import com.isaac.souqalghiyaradminnew.domain.model.SparePartCategory

enum class ConstantType { CATEGORY, QUALITY, BRAND, LOCATION }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConstantsScreen(
    viewModel: ConstantsViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val categories by viewModel.categories.collectAsState()
    val qualities by viewModel.qualities.collectAsState()
    val brands by viewModel.brands.collectAsState()
    val locations by viewModel.locations.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var currentConstantType by remember { mutableStateOf(ConstantType.CATEGORY) }
    var editId by remember { mutableStateOf<String?>(null) }
    var initialText by remember { mutableStateOf("") }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("إدارة الثوابت", color = Color.White, fontWeight = FontWeight.Bold) },
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
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // قسم أسماء القطع
                item {
                    SectionHeader("أقسام قطع الغيار") {
                        currentConstantType = ConstantType.CATEGORY
                        editId = null
                        initialText = ""
                        showDialog = true
                    }
                }
                items(categories) { category ->
                    ConstantListItem(
                        text = category.spare_parts_categories,
                        onEdit = {
                            currentConstantType = ConstantType.CATEGORY
                            editId = category.id
                            initialText = category.spare_parts_categories
                            showDialog = true
                        },
                        onDelete = { viewModel.deleteCategory(category.id) }
                    )
                }
                item { HorizontalDivider(color = Color.LightGray) }

                // قسم الماركات
                item {
                    SectionHeader("الماركات") {
                        currentConstantType = ConstantType.BRAND
                        editId = null
                        initialText = ""
                        showDialog = true
                    }
                }
                items(brands) { brand ->
                    ConstantListItem(
                        text = brand.brand_name,
                        onEdit = {
                            currentConstantType = ConstantType.BRAND
                            editId = brand.id
                            initialText = brand.brand_name
                            showDialog = true
                        },
                        onDelete = { viewModel.deleteBrand(brand.id) }
                    )
                }
                item { HorizontalDivider(color = Color.LightGray) }

                // قسم الجودة
                item {
                    SectionHeader("أنواع الجودة") {
                        currentConstantType = ConstantType.QUALITY
                        editId = null
                        initialText = ""
                        showDialog = true
                    }
                }
                items(qualities) { quality ->
                    ConstantListItem(
                        text = quality.quality_types,
                        onEdit = {
                            currentConstantType = ConstantType.QUALITY
                            editId = quality.id
                            initialText = quality.quality_types
                            showDialog = true
                        },
                        onDelete = { viewModel.deleteQualityType(quality.id) }
                    )
                }
                item { HorizontalDivider(color = Color.LightGray) }

                // قسم المحافظات
                item {
                    SectionHeader("المحافظات") {
                        currentConstantType = ConstantType.LOCATION
                        editId = null
                        initialText = ""
                        showDialog = true
                    }
                }
                items(locations) { location ->
                    ConstantListItem(
                        text = location.location,
                        onEdit = {
                            currentConstantType = ConstantType.LOCATION
                            editId = location.id
                            initialText = location.location
                            showDialog = true
                        },
                        onDelete = { viewModel.deleteLocation(location.id) }
                    )
                }
            }

            if (showDialog) {
                AddConstantDialog(
                    title = if (editId != null) "تعديل البيانات" else "إضافة جديدة",
                    initialValue = initialText,
                    onDismiss = { showDialog = false },
                    onConfirm = { name ->
                        when (currentConstantType) {
                            ConstantType.CATEGORY -> if (editId != null) viewModel.updateCategory(editId!!, name) else viewModel.addCategory(name)
                            ConstantType.QUALITY -> if (editId != null) viewModel.updateQualityType(editId!!, name) else viewModel.addQualityType(name)
                            ConstantType.BRAND -> if (editId != null) viewModel.updateBrand(editId!!, name) else viewModel.addBrand(name)
                            ConstantType.LOCATION -> if (editId != null) viewModel.updateLocation(editId!!, name) else viewModel.addLocation(name)
                        }
                        showDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, onAddClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF0D1B6D))
        IconButton(onClick = onAddClick) {
            Icon(Icons.Default.Add, contentDescription = "إضافة", tint = Color(0xFF4CAF50))
        }
    }
}

@Composable
fun ConstantListItem(text: String, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text, fontSize = 16.sp)
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Red)
            }
        }
    }
}

@Composable
fun AddConstantDialog(title: String, initialValue: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf(initialValue) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("الاسم") },
                singleLine = true
            )
        },
        confirmButton = { Button(onClick = { if (text.isNotBlank()) onConfirm(text) }) { Text("حفظ") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}
