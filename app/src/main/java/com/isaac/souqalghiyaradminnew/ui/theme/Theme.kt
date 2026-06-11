package com.isaac.souqalghiyaradminnew.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// تأكد من وجود ملف Color.kt يحتوي على هذه الألوان لكي لا يظهر لك خطأ هنا
private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    secondary = BlueSecondary,
    background = BackgroundColor,
    surface = White,
    onPrimary = White,
    onBackground = TextColor
)

@Composable
fun SuqalghiyarAdminTheme( // 👈 تم تعديل الاسم هنا ليطابق الـ MainActivity
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme // يمكنك إضافة DarkColorScheme هنا

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // تأكد من وجود ملف Type.kt
        content = content
    )
}