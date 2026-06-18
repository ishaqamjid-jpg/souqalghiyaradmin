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
    private val ROW_LINE_COLOR = Color.parseColor("#EEEEEE")

    fun generateFilteredReportPdf(context: Context, data: List<OrderWithItems>) {
        val pdfDocument = PdfDocument()
        var pageNumber = 1
        var yPosition = 180f
        val PAGE_BREAK_Y = 750f

        var pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        val paint = Paint()
        val textPaint = Paint().apply { color = TEXT_BLACK; textSize = 12f; textAlign = Paint.Align.RIGHT }
        val headerPaint = Paint().apply { color = Color.WHITE; textSize = 14f; isFakeBoldText = true; textAlign = Paint.Align.RIGHT }
        
        fun drawHeader(canvas: Canvas) {
            paint.color = MAIN_BLUE
            canvas.drawRect(0f, 0f, 595f, 80f, paint)
            headerPaint.textSize = 24f
            canvas.drawText("تقرير طلبات سوق الغيار", 570f, 50f, headerPaint)
            headerPaint.textSize = 14f

            // رسم خلفية عناوين الجدول
            paint.color = MAIN_BLUE
            canvas.drawRect(20f, 130f, 575f, 160f, paint)
            
            // عناوين الأعمدة (من اليمين لليسار)
            canvas.drawText("رقم الطلب", 565f, 150f, headerPaint)
            canvas.drawText("القطعة", 450f, 150f, headerPaint)
            canvas.drawText("التاجر", 300f, 150f, headerPaint)
            canvas.drawText("السعر", 180f, 150f, headerPaint)
            canvas.drawText("الحالة", 80f, 150f, headerPaint)
        }

        drawHeader(canvas)

        data.forEach { orderData ->
            val orderIdStr = orderData.order.order_id.take(8) // أول 8 حروف لتصغير المساحة
            val status = orderData.order.order_status
            
            orderData.items.forEach { item ->
                if (yPosition > PAGE_BREAK_Y) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    yPosition = 180f
                    drawHeader(canvas)
                }

                // رسم خط الصف
                paint.color = ROW_LINE_COLOR
                canvas.drawRect(20f, yPosition - 15f, 575f, yPosition + 10f, paint)

                // رسم البيانات
                canvas.drawText(orderIdStr, 565f, yPosition, textPaint)
                canvas.drawText(item.part_name, 450f, yPosition, textPaint)
                canvas.drawText(item.provider_name.ifEmpty { "غير محدد" }, 300f, yPosition, textPaint)
                canvas.drawText("${item.selling_price} ر.ي", 180f, yPosition, textPaint)
                canvas.drawText(status, 80f, yPosition, textPaint)

                yPosition += 30f
            }
        }

        pdfDocument.finishPage(page)
        savePdfToStorage(context, pdfDocument)
    }

    private fun savePdfToStorage(context: Context, doc: PdfDocument) {
        try {
            val fileName = "Report_${System.currentTimeMillis()}.pdf"
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
                Toast.makeText(context, "تم حفظ التقرير بنجاح في المستندات", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "خطأ في حفظ الملف!", Toast.LENGTH_SHORT).show()
        } finally {
            doc.close()
        }
    }
}
