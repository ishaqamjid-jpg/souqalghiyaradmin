package com.isaac.souqalghiyaradminnew.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
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

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override suspend fun loginAdmin(email: String, password: String): UserEmp? {
        return try {
            // 1. تسجيل الدخول باستخدام Firebase Auth
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            
            if (authResult.user != null) {
                // 2. إذا نجح الدخول، نجلب بيانات وصلاحيات الموظف من جدول UserEmp بواسطة الإيميل
                val snapshot = db.collection("UserEmp")
                    .whereEqualTo("email", email)
                    .whereEqualTo("status", "active")
                    .get()
                    .await()

                if (!snapshot.isEmpty) {
                    val doc = snapshot.documents.first()
                    // نأخذ الـ ID الخاص بـ Document وليس الـ Auth UID لضمان عمل باقي النظام
                    doc.toObject(UserEmp::class.java)?.copy(user_id = doc.id)
                } else {
                    Log.w("LoginDebug", "تم تسجيل الدخول لكن لا يوجد حساب موظف نشط بهذا الإيميل في قاعدة البيانات.")
                    auth.signOut() // نخرجه إذا لم يكن مسجلاً كموظف نشط
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("LoginError", "فشل تسجيل الدخول: ${e.message}", e)
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
