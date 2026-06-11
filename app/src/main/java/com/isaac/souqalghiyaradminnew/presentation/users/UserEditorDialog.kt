package com.isaac.souqalghiyaradminnew.presentation.users

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.isaac.souqalghiyaradminnew.domain.model.UserEmp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

@Composable
fun UserEditorDialog(
    user: UserEmp?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String) -> Unit
) {
    var displayName by remember { mutableStateOf(user?.display_name ?: "") }
    var phoneNumber by remember { mutableStateOf(user?.phone_number ?: "") }
    var permissions by remember { mutableStateOf(user?.user_permissions ?: "employee") }
    var status by remember { mutableStateOf(user?.status ?: "active") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (user == null) "إضافة موظف جديد" else "تعديل بيانات الموظف") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("الاسم الكامل") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("رقم الهاتف") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Text("الصلاحية:", fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = permissions == "admin", onClick = { permissions = "admin" })
                    Text("مدير")
                    Spacer(Modifier.width(16.dp))
                    RadioButton(selected = permissions == "employee", onClick = { permissions = "employee" })
                    Text("موظف")
                }

                Text("حالة الحساب:", fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = status == "active", onClick = { status = "active" })
                    Text("نشط")
                    Spacer(Modifier.width(16.dp))
                    RadioButton(selected = status == "not_active", onClick = { status = "not_active" })
                    Text("موقوف", color = Color.Red)
                }
            }
        },
        confirmButton = {
            Button(onClick = { 
                if(displayName.isNotBlank() && phoneNumber.isNotBlank()) {
                    onSave(user?.user_id ?: "", displayName, phoneNumber, permissions, status) 
                }
            }) {
                Text("حفظ")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}