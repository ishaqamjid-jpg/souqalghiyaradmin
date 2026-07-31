package com.isaac.souqalghiyaradminnew.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.isaac.souqalghiyaradminnew.MainActivity
import com.isaac.souqalghiyaradminnew.R

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // حفظ التوكن محلياً لتحديثه في قاعدة البيانات لاحقاً
        val sharedPref = getSharedPreferences("admin_prefs", Context.MODE_PRIVATE)
        sharedPref.edit().putString("fcm_token", token).apply()
        Log.d("FCM_TOKEN", "New Token Generated: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // الأفضل دائماً الاعتماد على Data payload لضمان العمل في الخلفية
        val title = remoteMessage.data["title"] ?: remoteMessage.notification?.title ?: "إشعار جديد"
        val message = remoteMessage.data["message"] ?: remoteMessage.notification?.body ?: "لديك تنبيه جديد من النظام"

        // التعديل هنا: استخراج رقم الطلب من الـ Payload
        val orderNumber = remoteMessage.data["order_number"] ?: ""

        showNotification(title, message, orderNumber)
    }

    // التعديل هنا: استقبال orderNumber كـ Parameter
    private fun showNotification(title: String, message: String, orderNumber: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            // التعديل هنا: تمرير رقم الطلب للـ Activity
            putExtra("order_number", orderNumber)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(), // لضمان عدم استبدال الإشعارات المتعددة
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE // استخدام UPDATE_CURRENT
        )

        val channelId = "admin_notifications_channel"

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            // ⚠️ تأكد أن الأيقونة مفرغة ومناسبة للإشعارات
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "إشعارات الطلبات والإدارة",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "قناة مخصصة لاستقبال تنبيهات الطلبات الجديدة من العملاء"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}