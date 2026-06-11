package com.isaac.souqalghiyaradminnew.domain.model
import com.google.firebase.Timestamp


data class Order(
    val order_id: String = "",
    val user_id: String = "",
    val brand_name: String = "",
    val vehicle_name: String = "",
    val vehicle_model: String = "",
    val manufacture: String = "",
    val vin_number: String = "",
    val delivery_location: String = "",
    val delivery_fees: Double = 0.0,
    val order_status: String = "pending",
    val created_at: Timestamp? = null
)

// 2. جدول القطع داخل الطلب
data class OrderItem(
    val item_id: String = "",
    val part_name: String = "",
    val quantity: Int = 1,
    val quality_type: String = "",
    val description: String = "",
    val comments: String = "",
    val purchase_price: Double = 0.0,
    val provider_name: String = "",
    val invoice_number: String = "",
    val selling_price: Double = 0.0
)