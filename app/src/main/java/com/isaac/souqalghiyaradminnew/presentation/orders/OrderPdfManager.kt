package com.isaac.souqalghiyaradminnew.presentation.orders

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.isaac.souqalghiyaradminnew.R
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

        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // مقاس A4 قياسي
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        // --- الألوان والخطوط ---
        val primaryColor = Color.parseColor("#0D1B6D")
        val secondaryColor = Color.parseColor("#42A5F5")

        val titlePaint = Paint().apply {
            color = primaryColor
            textSize = 26f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val datePaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 12f
            textAlign = Paint.Align.LEFT
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
            color = secondaryColor
            strokeWidth = 2f
        }
        val tableHeaderBgPaint = Paint().apply {
            color = Color.parseColor("#EEEEEE")
        }

        var startY = 40f

        // --- 1. رأس التقرير (التاريخ والشعار) ---
        val dateString = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("التاريخ: $dateString", 40f, startY + 20f, datePaint)

        // طباعة الشعار مع الحفاظ على التناسب (Aspect Ratio) وتوسيطه
        try {
            val logoBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.logo3)
            if (logoBitmap != null) {
                // تحديد عرض مناسب للشعار، وحساب الطول تلقائياً للحفاظ على الدقة
                val targetWidth = 140
                val aspectRatio = logoBitmap.width.toFloat() / logoBitmap.height.toFloat()
                val targetHeight = (targetWidth / aspectRatio).toInt()

                // true تعني فلترة الصورة لتظهر بدقة أعلى عند تغيير حجمها
                val scaledLogo = Bitmap.createScaledBitmap(logoBitmap, targetWidth, targetHeight, true)

                // حساب نقطة X ليكون الشعار في المنتصف تماماً
                val logoX = (pageInfo.pageWidth - targetWidth) / 2f
                canvas.drawBitmap(scaledLogo, logoX, startY, null)

                // النزول لأسفل بمقدار طول الشعار + مسافة إضافية
                startY += targetHeight + 25f
            } else {
                startY += 40f
            }
        } catch (e: Exception) {
            e.printStackTrace()
            startY += 40f
        }

        // رسم عنوان الفاتورة والخط الفاصل تحت الشعار
        canvas.drawText("فاتورة طلب قطع غيار", pageInfo.pageWidth / 2f, startY, titlePaint)

        startY += 20f
        canvas.drawLine(40f, startY, pageInfo.pageWidth - 40f, startY, linePaint)

        // --- 2. بيانات الفاتورة الأساسية ---
        startY += 40f
        canvas.drawText("رقم الطلب: ${order.order_number}", pageInfo.pageWidth - 40f, startY, headerPaint)
        startY += 30f
        canvas.drawText("المركبة: ${order.vehicle_name} - ${order.vehicle_model}", pageInfo.pageWidth - 40f, startY, normalPaint)
        startY += 25f
        canvas.drawText("الماركة: ${order.brand_name} | الصنع: ${order.manufacture}", pageInfo.pageWidth - 40f, startY, normalPaint)
        startY += 25f
        canvas.drawText("الموقع: ${order.delivery_location}", pageInfo.pageWidth - 40f, startY, normalPaint)

        startY += 40f
        canvas.drawLine(40f, startY, pageInfo.pageWidth - 40f, startY, linePaint)
        startY += 30f

        // --- 3. جدول القطع ---
        canvas.drawText("تفاصيل القطع المعتمدة:", pageInfo.pageWidth - 40f, startY, headerPaint)
        startY += 20f

        canvas.drawRect(40f, startY, pageInfo.pageWidth - 40f, startY + 30f, tableHeaderBgPaint)

        val tableHeaderPaint = Paint(headerPaint).apply { textSize = 12f; color = primaryColor }
        startY += 20f
        canvas.drawText("اسم القطعة (الجودة)", pageInfo.pageWidth - 50f, startY, tableHeaderPaint)
        canvas.drawText("الكمية", 250f, startY, tableHeaderPaint)
        canvas.drawText("السعر", 160f, startY, tableHeaderPaint)
        canvas.drawText("الإجمالي", 80f, startY, tableHeaderPaint)

        startY += 15f
        canvas.drawLine(40f, startY, pageInfo.pageWidth - 40f, startY, linePaint)
        startY += 25f

        val tableContentPaint = Paint(normalPaint).apply { textSize = 12f }
        var totalSellingPrice = 0.0

        items.forEachIndexed { index, item ->
            if (index % 2 == 1) {
                canvas.drawRect(40f, startY - 15f, pageInfo.pageWidth - 40f, startY + 10f, Paint().apply { color = Color.parseColor("#FAFAFA") })
            }

            val itemTotal = item.selling_price * item.quantity
            canvas.drawText("${item.part_name} (${item.quality_type})", pageInfo.pageWidth - 50f, startY, tableContentPaint)
            canvas.drawText("${item.quantity}", 250f, startY, tableContentPaint)
            canvas.drawText("${item.selling_price}", 160f, startY, tableContentPaint)
            canvas.drawText("$itemTotal", 80f, startY, tableContentPaint)

            totalSellingPrice += itemTotal
            startY += 25f
        }

        startY += 10f
        canvas.drawLine(40f, startY, pageInfo.pageWidth - 40f, startY, linePaint)
        startY += 40f

        // --- 4. المجاميع والرسوم ---
        totalSellingPrice += order.delivery_fees
        canvas.drawText("رسوم التوصيل: ${order.delivery_fees}", pageInfo.pageWidth - 40f, startY, headerPaint)
        startY += 35f

        val totalBoxPaint = Paint().apply { color = Color.parseColor("#E8F5E9") }
        canvas.drawRect(pageInfo.pageWidth - 250f, startY - 25f, pageInfo.pageWidth - 40f, startY + 15f, totalBoxPaint)

        val totalPaint = Paint(headerPaint).apply {
            textSize = 16f
            color = Color.parseColor("#2E7D32")
        }
        canvas.drawText("الإجمالي الكلي: $totalSellingPrice ريال", pageInfo.pageWidth - 50f, startY, totalPaint)

        pdfDocument.finishPage(page)

        val baseDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val souqDir = File(baseDir, "Souqfiles")
        if (!souqDir.exists()) souqDir.mkdirs()

        val fileName = "Invoice_${order.order_number}_${System.currentTimeMillis()}.pdf"
        val file = File(souqDir, fileName)

        try {
            pdfDocument.writeTo(FileOutputStream(file))
            sharePdf(context, file)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "حدث خطأ أثناء حفظ الـ PDF: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            pdfDocument.close()
        }
    }

    private fun sharePdf(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(shareIntent, "مشاركة الفاتورة عبر:")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            try {
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(viewIntent)
            } catch (ex: Exception) {
                Toast.makeText(context, "تم حفظ الفاتورة بنجاح في التنزيلات", Toast.LENGTH_LONG).show()
            }
        }
    }
}