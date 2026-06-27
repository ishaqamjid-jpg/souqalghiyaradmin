package com.isaac.souqalghiyaradminnew.presentation.settings

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Code
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.hilt.navigation.compose.hiltViewModel

// ViewModel الخاص بالنسخ الاحتياطي لربط العمليات
@HiltViewModel
class BackupViewModel @Inject constructor() : ViewModel() {
    
    fun exportAsExcel(onResult: (String) -> Unit) {
        viewModelScope.launch {
            // هنا سيتم كتابة كود جلب البيانات وتحويلها لـ CSV (Excel)
            onResult("تم تجهيز النسخة بصيغة Excel (CSV) بنجاح!")
        }
    }

    fun exportAsAppFormat(onResult: (String) -> Unit) {
        viewModelScope.launch {
            // هنا سيتم كتابة كود جلب البيانات وتحويلها لـ JSON (صيغة التطبيق)
            onResult("تم تجهيز النسخة بصيغة التطبيق (JSON) بنجاح!")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    viewModel: BackupViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("النسخة الاحتياطية", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, contentDescription = "", tint = Color.White) }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E1E1E))
                )
            },
            containerColor = Color(0xFF121212)
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.CloudDownload, contentDescription = "", tint = Color(0xFF4CAF50), modifier = Modifier.size(100.dp))
                Spacer(Modifier.height(24.dp))
                Text("اختر صيغة استخراج النسخة الاحتياطية:", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(32.dp))

                // زر استخراج بصيغة Excel
                Button(
                    onClick = {
                        viewModel.exportAsExcel { msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF217346)), // لون الإكسل الأخضر
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp)
                ) {
                    Icon(Icons.Default.TableChart, contentDescription = "Excel", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("استخراج بصيغة Excel (CSV)", fontSize = 16.sp)
                }

                Spacer(Modifier.height(16.dp))

                // زر استخراج بصيغة التطبيق
                Button(
                    onClick = {
                        viewModel.exportAsAppFormat { msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)), // لون التطبيق
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp)
                ) {
                    Icon(Icons.Default.Code, contentDescription = "App Format", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("استخراج بصيغة التطبيق (JSON)", fontSize = 16.sp)
                }
            }
        }
    }
}
