package com.isaac.souqalghiyaradminnew.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.isaac.souqalghiyaradminnew.domain.model.UserEmp
import com.isaac.souqalghiyaradminnew.domain.repository.AdminRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AdminRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : AdminRepository {

    // دالة تسجيل الدخول: تبحث برقم الهاتف وتتأكد أن الحساب active
    override suspend fun loginAdmin(phoneNumber: String): UserEmp? {
        return try {
            val snapshot = db.collection("users_emp")
                .whereEqualTo("phone_number", phoneNumber)
                .whereEqualTo("status", "active") // شرط أمني: يجب أن يكون الحساب نشطاً للدخول
                .get()
                .await()

            if (!snapshot.isEmpty) {
                val doc = snapshot.documents.first()
                // نأخذ الـ Document ID العشوائي ونضعه في حقل user_id
                doc.toObject(UserEmp::class.java)?.copy(user_id = doc.id)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // دالة المراقبة اللحظية لحالة وصلاحيات الموظف أثناء فتح التطبيق
    override fun observeAdminProfile(userId: String): Flow<UserEmp?> = callbackFlow {
        val subscription = db.collection("users_emp").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val user = snapshot.toObject(UserEmp::class.java)?.copy(user_id = snapshot.id)
                    trySend(user).isSuccess
                } else {
                    trySend(null).isSuccess // إذا حُذِف المستند
                }
            }
        awaitClose { subscription.remove() }
    }
}
