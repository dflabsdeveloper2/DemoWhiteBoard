package com.orbys.demowhiteboard.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.orbys.demowhiteboard.databinding.ActivityDialogSaveWhiteboardBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class DialogSaveWhiteboard : AppCompatActivity() {

    private lateinit var binding: ActivityDialogSaveWhiteboardBinding

    private var fileToSave: File? = null
    private var defaultFolder =
        File(Environment.getExternalStorageDirectory(), "/ORBYS/ORBYS_Whiteboard")

    companion object {
        const val RESULT_CODE_DIALOG_SAVE = 122 // Código de resultado personalizado
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDialogSaveWhiteboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI()
    }

    private fun initUI() {
        val permissionGranted = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        if (permissionGranted) {
            initValues()
            initListenners()
        } else {
            Toast.makeText(this, "No tienes permisos para guardar", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun initValues() {
        binding.etFolderFile.setText(
            defaultFolder.path.replace(
                Environment.getExternalStorageDirectory().absolutePath,
                ""
            )
        )

        if (!defaultFolder.exists()) {
            defaultFolder.mkdirs()
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd_HHmmss")
        val currentTimeMillis = System.currentTimeMillis()
        val date = Date(currentTimeMillis)
        val name = dateFormat.format(date)

        binding.etNameFile.setText(name)

    }

    private fun initListenners() {
        binding.etFolderFile.setOnClickListener {
            val intentFileManager = Intent(this,DialogFilemanager::class.java)
            someActivityResultLauncher.launch(intentFileManager)
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }

        binding.btnOk.setOnClickListener {
            val dir = fileToSave?.absolutePath ?: defaultFolder.absolutePath
            val name = binding.etNameFile.text.toString()
            Log.d("SAVE", "dir: $dir")
            if (dir != null) {
                if (dir.isNotBlank() && name.isNotBlank()) {
                    val file = File(dir, "$name.orbys")

                    val intentData = Intent()
                    intentData.putExtra("fileSave", file.absolutePath)

                    setResult(RESULT_CODE_DIALOG_SAVE, intentData)
                    finish()
                } else {
                    Toast.makeText(this, "Rellena los campos", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private val someActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            //Dialog Wallpaper
            if (result.resultCode == DialogFilemanager.RESULT_CODE_DIALOG_FILEMANAGER) {
                val data: Intent? = result.data
                val dataString = data?.getStringExtra("directory").orEmpty()
                if (dataString.isNotBlank()) {
                    fileToSave = File(dataString)
                    val nameFile = fileToSave?.name
                    if (nameFile == Environment.getExternalStorageDirectory().name) {
                        binding.etFolderFile.setText("Local")
                    } else {
                        binding.etFolderFile.setText(nameFile)
                    }
                }
            }
        }
}