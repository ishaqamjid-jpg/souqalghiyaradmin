package com.isaac.souqalghiyaradminnew.presentation.clients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.isaac.souqalghiyaradminnew.domain.model.users
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClientUsersViewModel @Inject constructor() : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    
    // القائمة الأساسية لكل العملاء القادمين من فايربيز
    private val _allClients = MutableStateFlow<List<users>>(emptyList())
    
    // متغير نص البحث
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // القائمة المفلترة التي سيتم عرضها في الواجهة
    val clients: StateFlow<List<users>> = combine(_allClients, _searchQuery) { allClients, query ->
        if (query.isBlank()) {
            allClients // إذا كان البحث فارغاً نعرض الكل
        } else {
            // فلترة القائمة بناءً على رقم الهاتف
            allClients.filter { it.phone_number.contains(query, ignoreCase = true) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        fetchClients()
    }

    private fun fetchClients() {
        // المراقبة اللحظية لجدول العملاء (users)
        db.collection("users").addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            if (snapshot != null) {
                _allClients.value = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(users::class.java)?.copy(user_id = doc.id)
                }
            }
        }
    }

    // دالة لتحديث نص البحث عند الكتابة
    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    // دالة لحظر / فك حظر العميل بنقرة واحدة (الزر الخارجي)
    fun toggleClientStatus(clientId: String, currentStatus: String) {
        val newStatus = if (currentStatus == "active") "not_active" else "active"
        viewModelScope.launch {
            db.collection("users").document(clientId).update("status", newStatus)
        }
    }

    // الدالة: تحديث جميع بيانات العميل (بعد التعديل من البطاقة الموسعة)
    fun updateClientDetails(client: users) {
        viewModelScope.launch {
            val updateMap = mapOf(
                "display_name" to client.display_name,
                "phone_number" to client.phone_number,
                "fcm_token" to client.fcm_token,
                "status" to client.status,
                "number_of_rejections" to client.number_of_rejections
            )
            db.collection("users").document(client.user_id).update(updateMap)
        }
    }

    // --- الدوال الجديدة للإضافة والحذف ---

    // إضافة عميل جديد
    fun addClient(client: users) {
        viewModelScope.launch {
            // توليد معرف جديد تلقائي (Auto-ID) من فايربيز
            val newDocRef = db.collection("users").document()
            // نسخ العميل ووضع المعرف الجديد
            val newClient = client.copy(user_id = newDocRef.id)
            newDocRef.set(newClient)
        }
    }

    // حذف عميل
    fun deleteClient(clientId: String) {
        viewModelScope.launch {
            db.collection("users").document(clientId).delete()
        }
    }
}
