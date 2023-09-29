package com.orbys.demowhiteboard.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.core.view.isVisible
import com.orbys.demowhiteboard.R
import com.orbys.demowhiteboard.databinding.DialogExportImageBinding
import java.io.File
import java.io.FileOutputStream

class DialogExport(context: Context, private val bitmap: Bitmap):Dialog(context) {

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
            var extension = ".png"

            val format:Bitmap.CompressFormat = when (rgFormat) {
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
                    Bitmap.CompressFormat.PNG
                }
            }

            if(nameFile.isNotBlank() && folderName.isNotBlank()){
                try {
                    val file = File(Environment.getExternalStoragePublicDirectory(folderName), nameFile+extension)
                    val fileOutputStream = FileOutputStream(file)

                    bitmap.compress(format, 100, fileOutputStream)
                    fileOutputStream.close()

                    Toast.makeText(context, "Imagen guardada en ${file.absolutePath}", Toast.LENGTH_SHORT)
                        .show()
                }catch (e:Exception){
                    Toast.makeText(context, "Error al guardar la imagen", Toast.LENGTH_SHORT)
                        .show()
                }
                binding.pbLoadingDialogExported.isVisible = false
                dismiss()
            }else{
                Toast.makeText(context, "Rellena todos los campos", Toast.LENGTH_SHORT)
                    .show()
            }
            binding.pbLoadingDialogExported.isVisible = false
        }
    }
}