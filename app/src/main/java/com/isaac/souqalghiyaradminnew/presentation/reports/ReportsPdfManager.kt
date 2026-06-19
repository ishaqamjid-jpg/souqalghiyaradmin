package com.isaac.souqalghiyaradminnew.presentation.reports

import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.isaac.souqalghiyaradminnew.domain.model.OrderWithItems
import java.text.SimpleDateFormat
import java.util.*

object ReportsPdfManager {

    private val MAIN_BLUE = Color.parseColor("#0D1B6D")
    private val TEXT_BLACK = Color.parseColor("#212121")
    private val LIGHT_GRAY = Color.parseColor("#F5F5F5")
    private val BORDER_GRAY = Color.parseColor("#E0E0E0")

    fun generateFilteredReportPdf(context: Context, data: List<OrderWithItems>) {
        val pdfDocument = PdfDocument()
        var pageNumber = 1
        var yPosition = 120f
        val PAGE_BREAK_Y = 780f

        var pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        val paint = Paint()
        val textPaint = Paint().apply { color = TEXT_BLACK; textSize = 10f; textAlign = Paint.Align.RIGHT }
        val boldTextPaint = Paint().apply { color = TEXT_BLACK; textSize = 10f; isFakeBoldText = true; textAlign = Paint.Align.RIGHT }
        val headerPaint = Paint().apply { color = Color.WHITE; textSize = 20f; isFakeBoldText = true; textAlign = Paint.Align.RIGHT }
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.ENGLISH)

        fun drawPageHeader(canvas: Canvas) {
            paint.color = MAIN_BLUE
            canvas.drawRect(0f, 0f, 595f, 80f, paint)
            canvas.drawText("التقرير الشامل للطلبات - سوق الغيار", 570f, 50f, headerPaint)
        }

        fun checkPageBreak() {
            if (yPosition > PAGE_BREAK_Y) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPosition = 120f
                drawPageHeader(canvas)
            }
        }

        drawPageHeader(canvas)

        data.forEach { orderData ->
            checkPageBreak()
            
            val orderDate = when (val ts = orderData.order.created_at) {
                is com.google.firebase.Timestamp -> dateFormat.format(ts.toDate())
                else -> "غير محدد"
            }

            // رسم خلفية ترويسة الطلب
            paint.color = LIGHT_GRAY
            canvas.drawRect(20f, yPosition - 15f, 575f, yPosition + 40f, paint)
            paint.color = BORDER_GRAY
            paint.style = Paint.Style.STROKE
            canvas.drawRect(20f, yPosition - 15f, 575f, yPosition + 40f, paint)
            paint.style = Paint.Style.FILL

            // السطر الأول
            // تم التعديل: إظهار order_number
            canvas.drawText("رقم الطلب: ${orderData.order.order_number}", 565f, yPosition, boldTextPaint)
            canvas.drawText("الحالة: ${orderData.order.order_status}", 350f, yPosition, textPaint)
            canvas.drawText("التاريخ: $orderDate", 180f, yPosition, textPaint)
            
            // السطر الثاني
            yPosition += 20f
            canvas.drawText("المركبة: ${orderData.order.vehicle_model} - ${orderData.order.manufacture}", 565f, yPosition, textPaint)
            // تم التعديل: إظهار vin_number للشاصي
            canvas.drawText("الشاصي: ${orderData.order.vin_number.ifEmpty { "لا يوجد" }}", 350f, yPosition, textPaint)
            canvas.drawText("رسوم التوصيل: ${orderData.order.delivery_fees} ر.ي", 180f, yPosition, textPaint)

            yPosition += 25f
            
            // رسم عناوين جدول القطع
            paint.color = MAIN_BLUE
            canvas.drawRect(20f, yPosition - 12f, 575f, yPosition + 10f, paint)
            val columnHeaderPaint = Paint().apply { color = Color.WHITE; textSize = 9f; isFakeBoldText = true; textAlign = Paint.Align.RIGHT }
            
            canvas.drawText("القطعة", 565f, yPosition, columnHeaderPaint)
            canvas.drawText("الكمية", 420f, yPosition, columnHeaderPaint)
            canvas.drawText("التاجر", 380f, yPosition, columnHeaderPaint)
            canvas.drawText("الفاتورة", 280f, yPosition, columnHeaderPaint)
            canvas.drawText("شراء", 190f, yPosition, columnHeaderPaint)
            canvas.drawText("بيع", 110f, yPosition, columnHeaderPaint)

            yPosition += 20f

            // رسم تفاصيل القطع
            orderData.items.forEach { item ->
                checkPageBreak()
                
                canvas.drawText(item.part_name, 565f, yPosition, textPaint)
                canvas.drawText(item.quantity.toString(), 420f, yPosition, textPaint)
                canvas.drawText(item.provider_name.ifEmpty { "غير محدد" }, 380f, yPosition, textPaint)
                canvas.drawText(item.invoice_number ?: "-", 280f, yPosition, textPaint)
                canvas.drawText("${item.purchase_price}", 190f, yPosition, textPaint)
                canvas.drawText("${item.selling_price}", 110f, yPosition, boldTextPaint)

                yPosition += 15f
            }

            // مسافة بين كل طلب والآخر
            yPosition += 20f
            paint.color = BORDER_GRAY
            canvas.drawLine(20f, yPosition, 575f, yPosition, paint) // خط فاصل خفيف
            yPosition += 20f
        }

        pdfDocument.finishPage(page)
        savePdfToStorage(context, pdfDocument)
    }

    private fun savePdfToStorage(context: Context, doc: PdfDocument) {
        try {
            val fileName = "FullReport_${System.currentTimeMillis()}.pdf"
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/SouqReports")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) 
                MediaStore.Files.getContentUri("external") 
            else 
                MediaStore.Files.getContentUri("external")
                
            val uri = context.contentResolver.insert(collection, contentValues)
            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { os -> doc.writeTo(os) }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    context.contentResolver.update(it, contentValues, null, null)
                }
                Toast.makeText(context, "تم تصدير التقرير بجميع بياناته بنجاح", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "خطأ في حفظ الملف!", Toast.LENGTH_SHORT).show()
        } finally {
            doc.close()
        }
    }
}
