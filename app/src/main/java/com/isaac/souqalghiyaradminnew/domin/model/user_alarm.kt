package com.isaac.souqalghiyaradminnew.domain.model

import com.google.firebase.Timestamp

data class user_alarm(
    val alarm_id: String = "",
    val title: String = "",
    val receiver_id: String = "",
    val order_number: Long = 0L,
    val message: String = "",
    val isRead: Boolean = false,
    val fcm_token: String = "",
    val date: Timestamp? = null
)
