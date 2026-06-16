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

    // أخذ نسخة من قاعدة بيانات فايرستور
    private val db = FirebaseFirestore.getInstance()
    
    // حالة قائمة الموظفين
    private val _users = MutableStateFlow<List<UserEmp>>(emptyList())
    val users: StateFlow<List<UserEmp>> = _users

    init {
        // جلب البيانات فور تهيئة الـ ViewModel
        fetchUsers()
    }

    // دالة لجلب البيانات بمراقبة لحظية (Real-time)
    private fun fetchUsers() {
        db.collection("UserEmp").addSnapshotListener { snapshot, error ->
            if (error != null) {
                // يمكنك هنا معالجة الأخطاء إذا لزم الأمر
                return@addSnapshotListener
            }
            if (snapshot != null) {
                _users.value = snapshot.documents.mapNotNull { doc ->
                    // ربط معرّف الوثيقة (Document ID) بحقل user_id
                    doc.toObject(UserEmp::class.java)?.copy(user_id = doc.id)
                }
            }
        }
    }

    // دالة لحفظ أو تحديث بيانات الموظف
    fun saveUser(
        id: String,
        displayName: String,
        phoneNumber: String,
        password: String,
        permissions: String,
        status: String
    ) {
        viewModelScope.launch {
            // تجهيز البيانات كـ Map لرفعها إلى الفايربيز
            val userMap = mutableMapOf<String, Any>(
                "display_name" to displayName,
                "phone_number" to phoneNumber,
                "password" to password,
                "user_permissions" to permissions,
                "status" to status
            )

            if (id.isEmpty()) {
                // إذا كان الـ ID فارغاً، فهذا مستخدم جديد
                // نضيف التوكن فارغاً افتراضياً مع تاريخ ووقت الإنشاء
                userMap["fcm_token"] = ""
                userMap["created_at"] = Timestamp.now()
                db.collection("UserEmp").add(userMap)
            } else {
                // إذا كان هناك ID، فهذه عملية تحديث لموظف موجود مسبقاً
                db.collection("UserEmp").document(id).update(userMap)
            }
        }
    }

    // دالة لحذف الموظف
    fun deleteUser(id: String) {
        viewModelScope.launch {
            if (id.isNotEmpty()) {
                db.collection("UserEmp").document(id).delete()
            }
        }
    }
}
