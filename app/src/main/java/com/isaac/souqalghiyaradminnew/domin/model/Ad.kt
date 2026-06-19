package com.isaac.souqalghiyaradminnew.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

data class Ad(
 @get:Exclude
 var ad_id: String = "",

 var title: String = "",
 var image_url: String = "",
 var click_action_type: String = "",
 var target_url: String? = null,
 var start_date: Timestamp? = null,
 var end_date: Timestamp? = null,
 var priority: Int = 0,

 // إجبار الفايربيز على استخدام هذا الاسم عند الرفع (get) وعند الجلب (set)
 @get:PropertyName("is_active")
 @set:PropertyName("is_active")
 var is_active: Boolean = true,

 var created_at: Timestamp? = null
)