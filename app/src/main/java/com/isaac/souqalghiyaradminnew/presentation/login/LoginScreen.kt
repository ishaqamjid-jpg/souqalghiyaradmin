package com.isaac.souqalghiyaradminnew.presentation.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.isaac.souqalghiyaradminnew.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    navigateToDashboard: (String, String, String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val phoneNumber by viewModel.phoneNumber.collectAsState()
    val password by viewModel.password.collectAsState()
    val rememberMe by viewModel.rememberMe.collectAsState()

    var showHelpDialog by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF070D2B), Color(0xFF15235B))))
        ) {
            IconButton(
                onClick = { showHelpDialog = true },
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
            ) {
                Icon(Icons.Default.HelpOutline, contentDescription = "مساعدة", tint = Color.White.copy(alpha = 0.8f))
            }

            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 30.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier
                        .size(130.dp)
                        .shadow(15.dp, CircleShape),
                    shape = CircleShape,
                    color = Color.White
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_admin), 
                        contentDescription = "شعار تطبيق الإدارة",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Fit
                    )
                }
                Spacer(Modifier.height(24.dp))
                
                Text(
                    text = "لوحة الإدارة - سوق الغيار", 
                    fontSize = 26.sp, 
                    fontWeight = FontWeight.ExtraBold, 
                    color = Color.White
                )
                
                Spacer(Modifier.height(8.dp))
                
                Text(
                    text = "سجل الدخول لإدارة التطبيق", 
                    fontSize = 14.sp, 
                    color = Color.White.copy(alpha = 0.7f)
                )
                
                Spacer(Modifier.height(40.dp))

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = viewModel::onPhoneNumberChange,
                    label = { Text("رقم هاتف الموظف") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White.copy(0.7f)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White.copy(0.7f),
                        focusedBorderColor = Color(0xFF4CAF50),
                        unfocusedBorderColor = Color.White.copy(0.5f),
                        cursorColor = Color.White
                    )
                )

                Spacer(Modifier.height(15.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = viewModel::onPasswordChange,
                    label = { Text("كلمة المرور") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White.copy(0.7f)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (!uiState.isLoading) {
                                viewModel.login { id, name, permissions -> navigateToDashboard(id, name, permissions) }
                            }
                        }
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White.copy(0.7f),
                        focusedBorderColor = Color(0xFF4CAF50),
                        unfocusedBorderColor = Color.White.copy(0.5f),
                        cursorColor = Color.White
                    )
                )

                Spacer(Modifier.height(15.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().clickable { viewModel.onRememberMeChange(!rememberMe) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { viewModel.onRememberMeChange(it) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF4CAF50),
                            uncheckedColor = Color.White.copy(alpha = 0.6f),
                            checkmarkColor = Color.White
                        )
                    )
                    Text("تذكرني في المرة القادمة", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.login { id, name, permissions -> navigateToDashboard(id, name, permissions) } },
                    modifier = Modifier.fillMaxWidth().height(55.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White, 
                        contentColor = Color(0xFF0D1B6D)
                    ),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = Color(0xFF0D1B6D))
                    } else {
                        Text("تسجيل الدخول", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }

                uiState.error?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = it, 
                        color = Color(0xFFFF5252), 
                        modifier = Modifier.padding(top = 10.dp), 
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        if (showHelpDialog) {
            AlertDialog(
                onDismissRequest = { showHelpDialog = false },
                title = { Text("تسجيل الدخول", fontWeight = FontWeight.Bold, color = Color(0xFF0D1B6D)) },
                text = { Text("قم بتسجيل الدخول باستخدام رقم الهاتف وكلمة المرور المسجلة مسبقاً للموظفين في قاعدة البيانات.") },
                confirmButton = {
                    TextButton(onClick = { showHelpDialog = false }) { 
                        Text("حسناً", color = Color(0xFF0D1B6D), fontWeight = FontWeight.Bold) 
                    }
                },
                containerColor = Color.White
            )
        }
    }
}
