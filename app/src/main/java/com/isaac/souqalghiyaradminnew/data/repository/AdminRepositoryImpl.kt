package com.isaac.souqalghiyaradminnew.data.repository

import android.util.Log
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

    override suspend fun loginAdmin(phoneNumber: String, password: String): UserEmp? {
        return try {
            // ملاحظة: تأكد أن اسم الجدول في Firebase هو "UserEmp" بنفس الحروف
            val snapshot = db.collection("UserEmp")
                .whereEqualTo("phone_number", phoneNumber)
                .whereEqualTo("password", password)
                .whereEqualTo("status", "active")
                .get()
                .await()

            if (!snapshot.isEmpty) {
                val doc = snapshot.documents.first()
                doc.toObject(UserEmp::class.java)?.copy(user_id = doc.id)
            } else {
                Log.w("LoginDebug", "لا يوجد مستخدم يطابق هذا الرقم أو كلمة المرور، أو الحساب غير نشط.")
                null
            }
        } catch (e: Exception) {
            // هذا السطر سيكشف لك إذا كانت المشكلة من صلاحيات الـ Rules
            Log.e("LoginError", "فشل الاتصال بقاعدة البيانات: ${e.message}", e)
            null
        }
    }

    override fun observeAdminProfile(userId: String): Flow<UserEmp?> = callbackFlow {
        val subscription = db.collection("UserEmp").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val user = snapshot.toObject(UserEmp::class.java)?.copy(user_id = snapshot.id)
                    trySend(user).isSuccess
                } else {
                    trySend(null).isSuccess
                }
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun updateFcmToken(userId: String, token: String): Result<Unit> {
        return try {
            db.collection("UserEmp").document(userId).update("fcm_token", token).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
