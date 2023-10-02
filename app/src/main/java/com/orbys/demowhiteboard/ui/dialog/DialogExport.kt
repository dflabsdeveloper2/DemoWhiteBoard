package com.orbys.demowhiteboard.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.view.isVisible
import com.orbys.demowhiteboard.R
import com.orbys.demowhiteboard.databinding.DialogExportImageBinding
import com.orbys.demowhiteboard.whiteboard.WriteBoard
import java.io.File
import java.io.FileOutputStream

class DialogExport(context: Context, private val whiteboard: WriteBoard):Dialog(context) {

    private lateinit var binding: DialogExportImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogExportImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnOk.setOnClickListener {
            binding.pbLoadingDialogExported.isVisible = true

            val nameFile = binding.etNameFile.text.toString()
            val folderName = /*binding.etFolderFile.text.toString()*/ "Picture/ORBYS_Whiteboard"
            val rgFormat = binding.rgButtonsFormat.checkedRadioButtonId
            val extension: String

            val format: Bitmap.CompressFormat? = when (rgFormat) {
                R.id.rbJPEG -> {
                    extension = ".jpeg"
                    Bitmap.CompressFormat.JPEG
                }

                R.id.rbPNG -> {
                    extension = ".png"
                    Bitmap.CompressFormat.PNG
                }

                else -> {
                    extension = ".pdf"
                    null
                }
            }

            if(nameFile.isNotBlank() && folderName.isNotBlank()){
                try {
                    //TODO: Falta programar lo de solo la pagina actual o todas
                    val whiteboardBitmap = Bitmap.createBitmap(
                        whiteboard.width,
                        whiteboard.height,
                        Bitmap.Config.ARGB_8888
                    )
                    val canvas = Canvas(whiteboardBitmap)
                    whiteboard.draw(canvas)

                    val file = File(
                        Environment.getExternalStoragePublicDirectory(folderName),
                        nameFile + extension
                    )

                    val created = if (format == null) {
                        convertBitmapToPdf(whiteboardBitmap,file)
                    } else {
                        val fileOutputStream = FileOutputStream(file)

                        whiteboardBitmap.compress(format, 100, fileOutputStream)
                        fileOutputStream.close()
                        true
                    }

                    if (created){
                        Toast.makeText(
                            context,
                            "Imagen guardada en ${file.absolutePath}",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }catch (e:Exception){
                    Toast.makeText(context, "Error al guardar la imagen", Toast.LENGTH_SHORT)
                        .show()
                }
                binding.pbLoadingDialogExported.isVisible = false
                dismiss()
            } else {
                Toast.makeText(context, "Rellena todos los campos", Toast.LENGTH_SHORT)
                    .show()
            }
            binding.pbLoadingDialogExported.isVisible = false
        }
    }

    private fun convertBitmapToPdf(bitmap: Bitmap, file: File): Boolean {
        val pdfDocument = PdfDocument()

        // Crea una página en el documento PDF
        val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
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
    }
}