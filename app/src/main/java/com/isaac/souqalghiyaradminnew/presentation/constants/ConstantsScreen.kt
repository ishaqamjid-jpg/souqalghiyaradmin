package com.isaac.souqalghiyaradminnew.presentation.constants

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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

enum class ConstantType { CATEGORY, BRAND, QUALITY, LOCATION }

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
    var editId by remember { mutableStateOf<String?>(null) }
    var initialText by remember { mutableStateOf("") }

    val tabs = listOf(
        Pair("أقسام قطع الغيار", ConstantType.CATEGORY),
        Pair("الماركات", ConstantType.BRAND),
        Pair("أنواع الجودة", ConstantType.QUALITY),
        Pair("المحافظات", ConstantType.LOCATION)
    )

    var selectedTabIndex by remember { mutableStateOf(0) }
    val currentType = tabs[selectedTabIndex].second

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        title = { Text("إدارة الثوابت", color = Color.White, fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = Color.White)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D1B6D))
                    )
                    ScrollableTabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = Color(0xFF0D1B6D),
                        contentColor = Color.White,
                        edgePadding = 8.dp,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = Color(0xFF4CAF50),
                                height = 3.dp
                            )
                        }
                    ) {
                        tabs.forEachIndexed { index, tab ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = { Text(tab.first, fontSize = 14.sp, fontWeight = if(selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal) },
                                unselectedContentColor = Color.LightGray,
                                selectedContentColor = Color.White
                            )
                        }
                    }
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        editId = null
                        initialText = ""
                        showDialog = true
                    },
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "إضافة جديد")
                }
            },
            containerColor = Color(0xFFF5F5F5)
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (currentType) {
                        ConstantType.CATEGORY -> {
                            if (categories.isEmpty()) item { EmptyStateMessage("لا توجد أقسام مضافة") }
                            items(categories) { category ->
                                ConstantListItem(
                                    text = category.spare_parts_categories,
                                    onEdit = { editId = category.id; initialText = category.spare_parts_categories; showDialog = true },
                                    onDelete = { viewModel.deleteCategory(category.id) }
                                )
                            }
                        }
                        ConstantType.BRAND -> {
                            if (brands.isEmpty()) item { EmptyStateMessage("لا توجد ماركات مضافة") }
                            items(brands) { brand ->
                                ConstantListItem(
                                    text = brand.brand_name,
                                    onEdit = { editId = brand.id; initialText = brand.brand_name; showDialog = true },
                                    onDelete = { viewModel.deleteBrand(brand.id) }
                                )
                            }
                        }
                        ConstantType.QUALITY -> {
                            if (qualities.isEmpty()) item { EmptyStateMessage("لا توجد أنواع جودة مضافة") }
                            items(qualities) { quality ->
                                ConstantListItem(
                                    text = quality.quality_types,
                                    onEdit = { editId = quality.id; initialText = quality.quality_types; showDialog = true },
                                    onDelete = { viewModel.deleteQualityType(quality.id) }
                                )
                            }
                        }
                        ConstantType.LOCATION -> {
                            if (locations.isEmpty()) item { EmptyStateMessage("لا توجد محافظات مضافة") }
                            items(locations) { location ->
                                ConstantListItem(
                                    text = location.location,
                                    onEdit = { editId = location.id; initialText = location.location; showDialog = true },
                                    onDelete = { viewModel.deleteLocation(location.id) }
                                )
                            }
                        }
                    }
                }
            }

            if (showDialog) {
                AddConstantDialog(
                    title = if (editId != null) "تعديل البيانات" else "إضافة جديدة",
                    initialValue = initialText,
                    onDismiss = { showDialog = false },
                    onConfirm = { name ->
                        when (currentType) {
                            ConstantType.CATEGORY -> if (editId != null) viewModel.updateCategory(editId!!, name) else viewModel.addCategory(name)
                            ConstantType.BRAND -> if (editId != null) viewModel.updateBrand(editId!!, name) else viewModel.addBrand(name)
                            ConstantType.QUALITY -> if (editId != null) viewModel.updateQualityType(editId!!, name) else viewModel.addQualityType(name)
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
fun EmptyStateMessage(message: String) {
    Box(modifier = Modifier.fillMaxSize().padding(top = 50.dp), contentAlignment = Alignment.Center) {
        Text(text = message, color = Color.Gray, fontSize = 16.sp)
    }
}

@Composable
fun ConstantListItem(text: String, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0D1B6D))
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(36.dp).background(Color(0xFFE3F2FD), CircleShape)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = Color(0xFF1976D2), modifier = Modifier.size(20.dp))
                }
                
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp).background(Color(0xFFFFEBEE), CircleShape)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Red, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun AddConstantDialog(title: String, initialValue: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf(initialValue) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold, color = Color(0xFF0D1B6D)) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("الاسم") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF0D1B6D),
                    focusedLabelColor = Color(0xFF0D1B6D)
                )
            )
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        confirmButton = {
            Button(
                onClick = { if (text.isNotBlank()) onConfirm(text) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("حفظ", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = Color.Gray, fontWeight = FontWeight.Bold)
            }
        }
    )
}
