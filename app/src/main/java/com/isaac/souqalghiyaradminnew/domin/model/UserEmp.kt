package com.isaac.souqalghiyaradminnew.domain.model

import com.google.firebase.Timestamp

data class UserEmp(
    val user_id: String = "",          // يتم ملؤه تلقائياً من الـ Document ID العشوائي
    val phone_number: String = "",
    val display_name: String = "",
    val fcm_token: String = "",
    val status: String = "active",       // "active" أو "not_active"
    val user_permissions: String = "employee", // "admin" أو "employee"
    val created_at: Timestamp? = null
)
