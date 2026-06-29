package com.isaac.souqalghiyaradminnew.presentation.settings

import android.content.Context
import android.os.Environment
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // قائمة بأسماء الجداول (Collections) في الفايربيز التي سيتم أخذ نسخة منها
    private val collectionsToBackup = listOf(
        "users", "users_emp", "orders", "order_items", 
        "advertisements", "brands", "location", 
        "quality_types", "spare_parts_categories"
    )

    fun exportBackup(context: Context, format: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val backupDataJson = JSONObject()
                val csvBuilder = StringBuilder()

                // المرور على جميع الجداول لجلبها من Firebase مباشرة
                for (collectionName in collectionsToBackup) {
                    val snapshot = db.collection(collectionName).get().await()

                    // مصفوفة للـ JSON
                    val docsArray = JSONArray()

                    // بناء ملف Excel (CSV)
                    if (snapshot.documents.isNotEmpty()) {
                        csvBuilder.append("--- جدول: $collectionName ---\n")
                        
                        // استخراج أسماء الأعمدة من أول مستند
                        val firstDoc = snapshot.documents.first().data ?: emptyMap<String, Any>()
                        val headers = firstDoc.keys.toList()
                        csvBuilder.append(headers.joinToString(",")).append("\n")

                        for (doc in snapshot.documents) {
                            val dataMap = doc.data ?: emptyMap()
                            
                            // إضافة للـ JSON
                            docsArray.put(JSONObject(dataMap))

                            // إضافة للـ Excel
                            val row = headers.map { key ->
                                val value = dataMap[key]?.toString() ?: ""
                                // تنظيف النص ليتناسب مع خلايا الإكسل
                                "\"${value.replace("\"", "\"\"").replace("\n", " ")}\""
                            }
                            csvBuilder.append(row.joinToString(",")).append("\n")
                        }
                        csvBuilder.append("\n\n") // مسافة بين الجداول في الإكسل
                    }
                    backupDataJson.put(collectionName, docsArray)
                }

                // إنشاء المجلد والحفظ في الهاتف (مجلد Documents)
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                if (!dir.exists()) dir.mkdirs()

                if (format == "json") {
                    val file = File(dir, "SouqAlghiyar_Backup_$timestamp.json")
                    FileWriter(file).use { it.write(backupDataJson.toString(4)) }
                    Toast.makeText(context, "تم حفظ نسخة التطبيق (JSON) في المستندات", Toast.LENGTH_LONG).show()
                } else if (format == "excel") {
                    val file = File(dir, "SouqAlghiyar_Backup_$timestamp.csv")
                    // إضافة (BOM) لكي يقرأ الإكسل اللغة العربية بشكل صحيح
                    val bom = "\uFEFF"
                    FileWriter(file).use { 
                        it.write(bom)
                        it.write(csvBuilder.toString()) 
                    }
                    Toast.makeText(context, "تم حفظ نسخة (Excel) في المستندات", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "حدث خطأ: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
