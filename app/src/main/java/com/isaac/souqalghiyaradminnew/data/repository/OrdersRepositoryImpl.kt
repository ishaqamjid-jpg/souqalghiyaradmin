    // --- 7. جلب كافة الطلبات بدون فلتر الحالة للتقارير ---
    override fun getAllOrdersForReports(): Flow<List<OrderWithItems>> = callbackFlow {
        val subscription = db.collection("orders")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val orderList = mutableListOf<OrderWithItems>()
                    if (snapshot.isEmpty) {
                        trySend(emptyList()).isSuccess
                        return@addSnapshotListener
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        snapshot.documents.forEach { doc ->
                            val order = doc.toObject(Order::class.java)?.copy(order_id = doc.id)
                            if (order != null) {
                                try {
                                    val itemsSnapshot = db.collection("orders").document(order.order_id).collection("items").get().await()
                                    val items = itemsSnapshot.documents.mapNotNull { itemDoc ->
                                        itemDoc.toObject(OrderItem::class.java)?.copy(item_id = itemDoc.id)
                                    }
                                    orderList.add(OrderWithItems(order, items))
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                        trySend(orderList.sortedByDescending { it.order.created_at.toString() }).isSuccess
                    }
                }
            }
        awaitClose { subscription.remove() }
    }
