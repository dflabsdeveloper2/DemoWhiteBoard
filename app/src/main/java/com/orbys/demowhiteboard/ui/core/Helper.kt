package com.orbys.demowhiteboard.ui.core

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.util.Base64

object Helper {

    fun createBitmapToBase64String(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.getEncoder().encodeToString(byteArray)
    }

    fun createBitmapFromBase64String(base64String: String): Bitmap {
        val byteArray = Base64.getDecoder().decode(base64String)
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    fun writeJsonToInternalFile(json: String, fileSaved: File) {
        try {
            Log.d("SAVE", "writeJsonToInternalFile: ${fileSaved.absolutePath}")

            val f = FileWriter(fileSaved)
            f.write(json)
            f.close()
        } catch (e: Exception) {
            Log.d("SAVE", "Exception to save: $e")
        }
    }

    fun createPdfWithBitmaps(bitmaps: List<Bitmap>, file: File) {
        // Crear un nuevo documento PDF
        val pdfDocument = PdfDocument()

        try {
            // Crear un archivo de salida para el PDF
            val outputStream = FileOutputStream(file)

            for (bitmap in bitmaps) {
                // Crear una página en el documento PDF
                val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
                val page = pdfDocument.startPage(pageInfo)

                // Dibujar el bitmap en la página
                val canvas = page.canvas
                canvas.drawBitmap(bitmap, 0f, 0f, null)

                // Finalizar la página
                pdfDocument.finishPage(page)
            }

            // Escribir el documento PDF en el archivo de salida

            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()
        }catch (e: Exception) {
            Log.d("Export", "Error crear PDF")
        }
    }

    /*fun convertBitmapToPdf(bitmap: Bitmap, file: File,pag:Int): Boolean {
        val pdfDocument = PdfDocument()

        // Crea una página en el documento PDF
        val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, pag).create()
        val page: PdfDocument.Page = pdfDocument.startPage(pageInfo)

        // Dibuja el Bitmap en la página PDF
        val canvas = page.canvas
        canvas.drawBitmap(bitmap, 0f, 0f, null)

        // Finaliza la página
        pdfDocument.finishPage(page)

        // Guarda el documento PDF en el almacenamiento externo
        try {
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()
        } catch (e: Exception) {
            Log.d("Export", "Error crear PDF")
        }

        return file.exists()
    }*/
}