package com.isaac.souqalghiyaradminnew.presentation.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.isaac.souqalghiyaradminnew.domain.model.UserEmp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UsersViewModel @Inject constructor() : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val _users = MutableStateFlow<List<UserEmp>>(emptyList())
    val users: StateFlow<List<UserEmp>> = _users

    init {
        fetchUsers()
    }

    private fun fetchUsers() {
        db.collection("users_emp").addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                _users.value = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(UserEmp::class.java)?.copy(user_id = doc.id)
                }
            }
        }
    }

    // دالة الحفظ تتوافق مع النموذج الجديد بالكامل
    fun saveUser(
        id: String,
        displayName: String,
        phoneNumber: String,
        permissions: String,
        status: String
    ) {
        viewModelScope.launch {
            val userMap = mutableMapOf<String, Any>(
                "display_name" to displayName,
                "phone_number" to phoneNumber,
                "user_permissions" to permissions,
                "status" to status
            )

            if (id.isEmpty()) {
                // مستخدم جديد: نضيف الـ fcm_token الافتراضي وتاريخ الإنشاء
                userMap["fcm_token"] = ""
                userMap["created_at"] = Timestamp.now()
                db.collection("users_emp").add(userMap)
            } else {
                // تحديث مستخدم حالي
                db.collection("users_emp").document(id).update(userMap)
            }
        }
    }

    fun deleteUser(id: String) {
        viewModelScope.launch {
            db.collection("users_emp").document(id).delete()
        }
    }
}
