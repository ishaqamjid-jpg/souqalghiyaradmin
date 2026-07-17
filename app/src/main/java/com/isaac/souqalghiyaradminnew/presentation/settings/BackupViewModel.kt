package com.isaac.souqalghiyaradminnew.presentation.settings

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileWriter
import java.io.InputStreamReader
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
        "users", "UserEmp", "orders", "order_items",
        "advertisements", "brands", "location",
        "quality_types", "spare_parts_categories"
    )

    fun exportBackup(context: Context, format: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val backupDataJson = JSONObject()
                val csvBuilder = StringBuilder()

                for (collectionName in collectionsToBackup) {
                    val snapshot = db.collection(collectionName).get().await()
                    val docsArray = JSONArray()

                    if (snapshot.documents.isNotEmpty()) {
                        csvBuilder.append("--- جدول: $collectionName ---\n")
                        val firstDoc = snapshot.documents.first().data ?: emptyMap<String, Any>()
                        val headers = firstDoc.keys.toList()
                        csvBuilder.append(headers.joinToString(",")).append("\n")

                        for (doc in snapshot.documents) {
                            val dataMap = doc.data ?: emptyMap()

                            // للـ JSON نحتفظ بـ Document ID لسهولة الاستعادة
                            val jsonDoc = JSONObject(dataMap)
                            jsonDoc.put("_doc_id_", doc.id)
                            docsArray.put(jsonDoc)

                            val row = headers.map { key ->
                                val value = dataMap[key]?.toString() ?: ""
                                "\"${value.replace("\"", "\"\"").replace("\n", " ")}\""
                            }
                            csvBuilder.append(row.joinToString(",")).append("\n")
                        }
                        csvBuilder.append("\n\n")
                    }
                    backupDataJson.put(collectionName, docsArray)
                }

                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

                // حفظ الملفات في مجلد التنزيلات داخل مجلد خاص Souqfiles
                val baseDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val souqDir = File(baseDir, "Souqfiles")
                if (!souqDir.exists()) souqDir.mkdirs()

                if (format == "json") {
                    val file = File(souqDir, "SouqAlghiyar_Backup_$timestamp.json")
                    FileWriter(file).use { it.write(backupDataJson.toString(4)) }
                    Toast.makeText(context, "تم الحفظ بنجاح في التنزيلات/Souqfiles", Toast.LENGTH_LONG).show()
                } else if (format == "excel") {
                    val file = File(souqDir, "SouqAlghiyar_Backup_$timestamp.csv")
                    val bom = "\uFEFF"
                    FileWriter(file).use {
                        it.write(bom)
                        it.write(csvBuilder.toString())
                    }
                    // تم تصحيح الخطأ المطبعي هنا
                    Toast.makeText(context, "تم حفظ نسخة Excel قابلة للتعديل في التنزيلات/Souqfiles", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "حدث خطأ: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // دالة استعادة البيانات من ملف JSON
    fun restoreBackupFromJson(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val jsonString = withContext(Dispatchers.IO) {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    reader.readText()
                }

                val backupJson = JSONObject(jsonString)

                db.runBatch { batch ->
                    for (collectionName in backupJson.keys()) {
                        val docsArray = backupJson.getJSONArray(collectionName)
                        for (i in 0 until docsArray.length()) {
                            val jsonDoc = docsArray.getJSONObject(i)
                            val docId = jsonDoc.optString("_doc_id_")
                            jsonDoc.remove("_doc_id_") // نزيل الـ ID من المحتوى

                            val dataMap = jsonToMap(jsonDoc)

                            val docRef = if (docId.isNotEmpty()) {
                                db.collection(collectionName).document(docId)
                            } else {
                                db.collection(collectionName).document()
                            }
                            batch.set(docRef, dataMap)
                        }
                    }
                }.await()

                Toast.makeText(context, "تم استعادة قاعدة البيانات بنجاح!", Toast.LENGTH_LONG).show()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "فشلت الاستعادة، تأكد من صحة الملف", Toast.LENGTH_LONG).show()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // دالة مساعدة لتحويل JSON إلى Map ليتوافق مع Firestore
    private fun jsonToMap(jsonObject: JSONObject): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        val keys = jsonObject.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = jsonObject.get(key)
            map[key] = if (value is JSONObject) jsonToMap(value) else value
        }
        return map
    }
}