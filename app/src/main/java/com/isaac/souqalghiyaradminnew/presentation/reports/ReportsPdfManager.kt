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
            val pdfDocument = PdfDocument()
            val pageWidth = 595 
            val pageHeight = 842 
            var pageNumber = 1

            val titlePaint = Paint().apply {
                textSize = 20f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = Color.rgb(13, 27, 109) 
                textAlign = Paint.Align.CENTER // لتوسيط العنوان كما في الفاتورة
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

            val successNotesPaint = Paint().apply {
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = Color.rgb(76, 175, 80)
                textAlign = Paint.Align.RIGHT
            }

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
            
            val topBorderPaint = Paint().apply {
                color = Color.parseColor("#42A5F5") 
                strokeWidth = 2f
            }

            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas

            val rightMargin = pageWidth - 40f
            val leftMargin = 40f
            var yPosition = 40f

            // --- 1. رأس التقرير (مشابه تماماً لـ OrderPdfManager) ---
            val currentDateString = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(java.util.Date())
            canvas.drawText("التاريخ: $currentDateString", leftMargin, yPosition + 20f, textPaint.apply { textAlign = Paint.Align.LEFT })

            try {
                // رسم الشعار
                val logoBitmap = android.graphics.BitmapFactory.decodeResource(context.resources, com.isaac.souqalghiyaradminnew.R.drawable.logo3)
                if (logoBitmap != null) {
                    val scaledLogo = android.graphics.Bitmap.createScaledBitmap(logoBitmap, 60, 60, false)
                    canvas.drawBitmap(scaledLogo, (pageWidth / 2f) - 30f, yPosition, null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            yPosition += 80f
            canvas.drawText("التقرير الشامل للطلبات - سوق الغيار", pageWidth / 2f, yPosition, titlePaint)
            
            yPosition += 20f
            canvas.drawLine(leftMargin, yPosition, rightMargin, yPosition, topBorderPaint)
            yPosition += 40f

            // إرجاع المحاذاة لليمين للنصوص العادية
            textPaint.textAlign = Paint.Align.RIGHT

            val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.ENGLISH)

            // --- 2. المرور على جميع الطلبات ---
            orders.forEach { orderData ->
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

                canvas.drawText("رقم الطلب: ${order.order_number} | الحالة: ${order.order_status}", rightMargin, yPosition, headerPaint)
                yPosition += 25f

                canvas.drawText("التاريخ: $orderDate", rightMargin, yPosition, textPaint)
                yPosition += 20f

                val vehicleInfo = "${order.brand_name} ${order.vehicle_name} ${order.vehicle_model} - ${order.manufacture}"
                canvas.drawText("المركبة: $vehicleInfo | رقم الشاصي: ${order.vin_number.ifEmpty { "غير متوفر" }}", rightMargin, yPosition, textPaint)
                yPosition += 20f

                canvas.drawText("رسوم التوصيل: ${order.delivery_fees} ر.ي", rightMargin, yPosition, textPaint)
                yPosition += 20f

                if (order.order_status.equals("completed", ignoreCase = true) && order.approval_notes.isNotBlank()) {
                    canvas.drawText("ملاحظات الموافقة: ${order.approval_notes}", rightMargin, yPosition, successNotesPaint)
                    yPosition += 20f
                } else if (order.order_status.equals("canceled", ignoreCase = true) && order.disapproval_notes.isNotBlank()) {
                    canvas.drawText("سبب الرفض: ${order.disapproval_notes}", rightMargin, yPosition, errorNotesPaint)
                    yPosition += 20f
                }

                canvas.drawText("القطع المطلوبة (${orderData.items.size}):", rightMargin, yPosition, headerPaint)
                yPosition += 20f

                orderData.items.forEach { item ->
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

                yPosition += 10f
                canvas.drawLine(leftMargin, yPosition, rightMargin, yPosition, dividerPaint)
                yPosition += 30f
            }

            pdfDocument.finishPage(page)

            val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            if (directory != null && !directory.exists()) {
                directory.mkdirs()
            }
            
            val fileName = "SouqAlghiyar_Report_${System.currentTimeMillis()}.pdf"
            val file = File(directory, fileName)
            
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()

            // 3. فتح خيارات المشاركة بدلاً من فتح الملف فقط
            sharePdf(context, file)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "حدث خطأ أثناء إنشاء الـ PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun sharePdf(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val chooser = Intent.createChooser(intent, "مشاركة التقرير عبر:")
            chooser.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "حدث خطأ أثناء فتح المشاركة، تأكد من FileProvider", Toast.LENGTH_LONG).show()
        }
    }
}
