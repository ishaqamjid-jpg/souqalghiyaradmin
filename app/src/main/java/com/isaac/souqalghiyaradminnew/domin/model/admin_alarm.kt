package com.isaac.souqalghiyaradminnew.domain.model

import com.google.firebase.Timestamp

data class admin_alarm(
    val alarm_id: String = "",
    val date: Timestamp? = null,
    val order_number: Long = 0L,
    val message: String = "",
    val title: String = "",
    val isRead: Boolean = false
)
