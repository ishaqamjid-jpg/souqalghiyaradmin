package com.isaac.souqalghiyaradminnew.presentation.reports

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.isaac.souqalghiyaradminnew.domain.model.OrderWithItems
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

object ReportsPdfManager {

    fun generateFilteredReportPdf(context: Context, orders: List<OrderWithItems>) {
        try {
            // 1. إعداد مستند الـ PDF
            val pdfDocument = PdfDocument()
            val pageWidth = 595 // عرض صفحة A4 تقريباً
            val pageHeight = 842 // طول صفحة A4 تقريباً
            var pageNumber = 1

            // 2. إعداد الخطوط والألوان (تم ضبط المحاذاة لليمين لدعم العربية)
            val titlePaint = Paint().apply {
                textSize = 20f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = Color.rgb(13, 27, 109) // لون أزرق غامق (نفس لون الـ TopBar)
                textAlign = Paint.Align.RIGHT
            }

            val headerPaint = Paint().apply {
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = Color.BLACK
                textAlign = Paint.Align.RIGHT
            }

            val textPaint = Paint().apply {
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                color = Color.DKGRAY
                textAlign = Paint.Align.RIGHT
            }

            // خط ملاحظات الموافقة (أخضر)
            val successNotesPaint = Paint().apply {
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = Color.rgb(76, 175, 80) // أخضر
                textAlign = Paint.Align.RIGHT
            }

            // خط أسباب الرفض (أحمر)
            val errorNotesPaint = Paint().apply {
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = Color.RED
                textAlign = Paint.Align.RIGHT
            }

            val dividerPaint = Paint().apply {
                color = Color.LTGRAY
                strokeWidth = 1f
            }

            // 3. إنشاء الصفحة الأولى
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas

            val rightMargin = pageWidth - 40f
            val leftMargin = 40f
            var yPosition = 60f

            // رسم العنوان الرئيسي
            canvas.drawText("التقرير الشامل للطلبات - سوق الغيار", rightMargin, yPosition, titlePaint)
            yPosition += 40f

            val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.ENGLISH)

            // 4. المرور على جميع الطلبات ورسمها
            orders.forEach { orderData ->
                // التحقق من المساحة المتبقية في الصفحة، وفتح صفحة جديدة إذا لزم الأمر
                if (yPosition > pageHeight - 150f) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    yPosition = 60f
                }

                val order = orderData.order
                val orderDate = when (val ts = order.created_at) {
                    is com.google.firebase.Timestamp -> dateFormat.format(ts.toDate())
                    else -> "غير محدد"
                }

                // بيانات الطلب الأساسية
                canvas.drawText("رقم الطلب: ${order.order_number} | الحالة: ${order.order_status}", rightMargin, yPosition, headerPaint)
                yPosition += 25f

                canvas.drawText("التاريخ: $orderDate", rightMargin, yPosition, textPaint)
                yPosition += 20f

                val vehicleInfo = "${order.brand_name} ${order.vehicle_name} ${order.vehicle_model} - ${order.manufacture}"
                canvas.drawText("المركبة: $vehicleInfo | رقم الشاصي: ${order.vin_number.ifEmpty { "غير متوفر" }}", rightMargin, yPosition, textPaint)
                yPosition += 20f

                canvas.drawText("رسوم التوصيل: ${order.delivery_fees} ر.ي", rightMargin, yPosition, textPaint)
                yPosition += 20f

                // ----------- إضافة الملاحظات وأسباب الرفض --------------
                if (order.order_status.equals("completed", ignoreCase = true) && order.approval_notes.isNotBlank()) {
                    canvas.drawText("ملاحظات الموافقة: ${order.approval_notes}", rightMargin, yPosition, successNotesPaint)
                    yPosition += 20f
                } else if (order.order_status.equals("canceled", ignoreCase = true) && order.disapproval_notes.isNotBlank()) {
                    canvas.drawText("سبب الرفض: ${order.disapproval_notes}", rightMargin, yPosition, errorNotesPaint)
                    yPosition += 20f
                }
                // -------------------------------------------------------

                // تفاصيل القطع
                canvas.drawText("القطع المطلوبة (${orderData.items.size}):", rightMargin, yPosition, headerPaint)
                yPosition += 20f

                orderData.items.forEach { item ->
                    // التحقق من الصفحة مرة أخرى بداخل حلقة القطع (إذا كان الطلب يحتوي على قطع كثيرة)
                    if (yPosition > pageHeight - 80f) {
                        pdfDocument.finishPage(page)
                        pageNumber++
                        pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                        page = pdfDocument.startPage(pageInfo)
                        canvas = page.canvas
                        yPosition = 60f
                    }

                    val itemDesc = "- ${item.part_name} | الكمية: ${item.quantity} | التاجر: ${item.provider_name.ifEmpty { "غير محدد" }}"
                    canvas.drawText(itemDesc, rightMargin - 20f, yPosition, textPaint)
                    yPosition += 20f
                    
                    val priceDesc = "  سعر الشراء: ${item.purchase_price} | سعر البيع: ${item.selling_price} | الفاتورة: ${item.invoice_number.ifEmpty { "-" }}"
                    canvas.drawText(priceDesc, rightMargin - 20f, yPosition, textPaint)
                    yPosition += 25f
                }

                // خط فاصل بين الطلبات
                yPosition += 10f
                canvas.drawLine(leftMargin, yPosition, rightMargin, yPosition, dividerPaint)
                yPosition += 30f
            }

            // 5. إنهاء الصفحة وحفظ المستند
            pdfDocument.finishPage(page)

            // تحديد مسار الحفظ في مجلد المستندات الخاص بالتطبيق
            val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            if (directory != null && !directory.exists()) {
                directory.mkdirs()
            }
            
            val fileName = "SouqAlghiyar_Report_${System.currentTimeMillis()}.pdf"
            val file = File(directory, fileName)
            
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()

            // 6. فتح الملف بعد الحفظ باستخدام الـ Intent
            openPdf(context, file)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "حدث خطأ أثناء إنشاء الـ PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun openPdf(context: Context, file: File) {
        try {
            // ملاحظة: تأكد من إضافة provider في ملف AndroidManifest.xml لتجنب أخطاء FileUriExposedException
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "تم الحفظ، لكن لم يتم العثور على تطبيق لفتح ملفات PDF", Toast.LENGTH_LONG).show()
        }
    }
}
