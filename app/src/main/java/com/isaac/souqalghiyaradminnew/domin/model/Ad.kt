package com.isaac.souqalghiyaradminnew.domain.model
import com.google.firebase.Timestamp



   data class Ad(
    val ad_id: String = "",
    val title: String = "",
    val image_url: String = "",
    val click_action_type: String = "",
    val target_url: String? = null,
    val start_date: Timestamp? = null,
    val end_date: Timestamp? = null,
    val priority: Int = 0,
    val is_active: Boolean = true,
    val created_at: Timestamp? = null
)