package com.isaac.souqalghiyaradminnew.presentation.clients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.isaac.souqalghiyaradminnew.domain.model.users
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClientUsersViewModel @Inject constructor() : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val _clients = MutableStateFlow<List<users>>(emptyList())
    val clients: StateFlow<List<users>> = _clients

    init {
        fetchClients()
    }

    private fun fetchClients() {
        // المراقبة اللحظية لجدول العملاء (users)
        db.collection("users").addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            if (snapshot != null) {
                _clients.value = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(users::class.java)?.copy(user_id = doc.id)
                }
            }
        }
    }

    // دالة لحظر / فك حظر العميل بنقرة واحدة
    fun toggleClientStatus(clientId: String, currentStatus: String) {
        val newStatus = if (currentStatus == "active") "not_active" else "active"
        viewModelScope.launch {
            db.collection("users").document(clientId).update("status", newStatus)
        }
    }
}

