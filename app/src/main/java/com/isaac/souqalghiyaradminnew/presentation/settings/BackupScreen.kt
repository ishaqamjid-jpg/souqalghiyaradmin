package com.isaac.souqalghiyaradminnew.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    onNavigateBack: () -> Unit,
    viewModel: BackupViewModel = hiltViewModel()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("النسخة الاحتياطية", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) { 
                            Icon(Icons.Default.ArrowBack, contentDescription = "", tint = Color.White) 
                        }
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
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CloudDownload, 
                    contentDescription = "Backup", 
                    tint = Color(0xFFE91E63), 
                    modifier = Modifier.size(120.dp)
                )
                
                Spacer(Modifier.height(24.dp))
                
                Text(
                    text = "سحب قاعدة البيانات كاملة من Firebase", 
                    color = Color.White, 
                    fontSize = 18.sp, 
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "اختر صيغة التصدير المناسبة لك ليتم حفظها في مجلد المستندات (Documents) في هاتفك.",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 32.dp),
                    textAlign = TextAlign.Center
                )

                if (isLoading) {
                    CircularProgressIndicator(color = Color(0xFFE91E63))
                    Spacer(Modifier.height(16.dp))
                    Text("جاري سحب البيانات من السيرفر...", color = Color.Gray)
                } else {
                    // زر التصدير بصيغة الإكسل
                    Button(
                        onClick = { viewModel.exportBackup(context, "excel") },
                        modifier = Modifier.fillMaxWidth().height(55.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("استخراج بصيغة Excel (للقراءة)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(16.dp))

                    // زر التصدير بصيغة JSON
                    Button(
                        onClick = { viewModel.exportBackup(context, "json") },
                        modifier = Modifier.fillMaxWidth().height(55.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(Icons.Default.Code, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("استخراج بصيغة JSON (للتطبيق)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
