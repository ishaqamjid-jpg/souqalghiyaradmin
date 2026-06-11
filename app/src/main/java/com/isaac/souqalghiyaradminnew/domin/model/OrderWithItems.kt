package com.isaac.souqalghiyaradminnew.domain.model

import com.google.firebase.Timestamp


data class OrderWithItems(
    val order: Order,
    val items: List<OrderItem>
)