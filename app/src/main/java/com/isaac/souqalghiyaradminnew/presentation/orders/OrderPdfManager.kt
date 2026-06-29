package com.isaac.souqalghiyaradminnew.presentation.orders

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import com.isaac.souqalghiyaradminnew.domain.model.OrderWithItems
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object OrderPdfManager {

    fun generateOrderPdf(context: Context, data: OrderWithItems) {
        val order = data.order
        val items = data.items

        // إعداد المستند
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // مقاس A4
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        // إعدادات الخطوط والألوان
        val titlePaint = Paint().apply {
            color = Color.parseColor("#0D1B6D")
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val headerPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 14f
            textAlign = Paint.Align.RIGHT
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val normalPaint = Paint().apply {
            color = Color.BLACK
            textSize = 14f
            textAlign = Paint.Align.RIGHT
        }
        val linePaint = Paint().apply {
            color = Color.parseColor("#42A5F5")
            strokeWidth = 2f
        }

        // 1. ترويسة الفاتورة
        canvas.drawText("فاتورة طلب قطع غيار", pageInfo.pageWidth / 2f, 60f, titlePaint)
        canvas.drawLine(40f, 80f, pageInfo.pageWidth - 40f, 80f, linePaint)

        // 2. معلومات الطلب والمركبة
        var startY = 120f
        canvas.drawText("رقم الطلب: ${order.order_number}", pageInfo.pageWidth - 40f, startY, headerPaint)
        startY += 30f
        canvas.drawText("المركبة: ${order.vehicle_name} - ${order.vehicle_model}", pageInfo.pageWidth - 40f, startY, normalPaint)
        startY += 25f
        canvas.drawText("الماركة: ${order.brand_name} | الصنع: ${order.manufacture}", pageInfo.pageWidth - 40f, startY, normalPaint)
        startY += 25f
        canvas.drawText("الموقع: ${order.delivery_location}", pageInfo.pageWidth - 40f, startY, normalPaint)
        startY += 25f
        
        val dateString = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("تاريخ الطباعة: $dateString", pageInfo.pageWidth - 40f, startY, normalPaint)

        startY += 40f
        canvas.drawLine(40f, startY, pageInfo.pageWidth - 40f, startY, linePaint)
        startY += 30f

        // 3. جدول القطع
        canvas.drawText("تفاصيل القطع:", pageInfo.pageWidth - 40f, startY, headerPaint)
        startY += 30f

        // رسم رأس الجدول
        val tableHeaderPaint = Paint(headerPaint).apply { textSize = 12f }
        canvas.drawText("اسم القطعة (الجودة)", pageInfo.pageWidth - 40f, startY, tableHeaderPaint)
        canvas.drawText("الكمية", 250f, startY, tableHeaderPaint)
        canvas.drawText("سعر الشراء", 160f, startY, tableHeaderPaint)
        canvas.drawText("سعر البيع", 80f, startY, tableHeaderPaint)
        
        startY += 10f
        canvas.drawLine(40f, startY, pageInfo.pageWidth - 40f, startY, Paint().apply { color = Color.LTGRAY; strokeWidth = 1f })
        startY += 25f

        // رسم عناصر الجدول
        val tableContentPaint = Paint(normalPaint).apply { textSize = 12f }
        var totalSellingPrice = 0.0

        items.forEach { item ->
            canvas.drawText("${item.part_name} (${item.quality_type})", pageInfo.pageWidth - 40f, startY, tableContentPaint)
            canvas.drawText("${item.quantity}", 250f, startY, tableContentPaint)
            canvas.drawText("${item.purchase_price}", 160f, startY, tableContentPaint)
            canvas.drawText("${item.selling_price}", 80f, startY, tableContentPaint)
            
            totalSellingPrice += (item.selling_price * item.quantity)
            startY += 25f
        }

        startY += 10f
        canvas.drawLine(40f, startY, pageInfo.pageWidth - 40f, startY, linePaint)
        startY += 30f

        // 4. المجاميع والرسوم
        totalSellingPrice += order.delivery_fees
        canvas.drawText("رسوم التوصيل: ${order.delivery_fees}", pageInfo.pageWidth - 40f, startY, headerPaint)
        startY += 30f
        
        val totalPaint = Paint(headerPaint).apply { 
            textSize = 16f
            color = Color.parseColor("#4CAF50") // لون أخضر للمبلغ الإجمالي
        }
        canvas.drawText("الإجمالي المطلوب من العميل: $totalSellingPrice", pageInfo.pageWidth - 40f, startY, totalPaint)

        // إنهاء الصفحة
        pdfDocument.finishPage(page)

        // 5. حفظ الملف في مجلد التنزيلات أو المستندات
        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        if (!directory.exists()) directory.mkdirs()

        val fileName = "Invoice_${order.order_number}_${System.currentTimeMillis()}.pdf"
        val file = File(directory, fileName)

        try {
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(context, "تم حفظ الفاتورة بنجاح في المستندات (Documents)", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "حدث خطأ أثناء حفظ الـ PDF: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            pdfDocument.close()
        }
    }
}
