package com.orbys.demowhiteboard.ui

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.orbys.demowhiteboard.databinding.ActivityDialogExportBinding
import com.orbys.demowhiteboard.ui.core.Util
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class DialogExport : AppCompatActivity() {

    private lateinit var binding: ActivityDialogExportBinding

    private var fileToSave: File? = null

    companion object{
        const val RESULT_CODE_DIALOG_EXPORT = 15723 // Código de resultado personalizado
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDialogExportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI()
    }

    private fun initUI() {
        initValues()
        initListenners()
    }

    private fun initValues() {
        binding.etFolderFile.setText(
            Util.defaultFolder.path.replace(
                Environment.getExternalStorageDirectory().absolutePath, ""
            )
        )

        if (!Util.defaultFolder.exists()) {
            Util.defaultFolder.mkdirs()
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd_HHmmss")
        val currentTimeMillis = System.currentTimeMillis()
        val date = Date(currentTimeMillis)
        val name = dateFormat.format(date)

        binding.etNameFile.setText(name)
    }

    private fun initListenners() {
        binding.etFolderFile.setOnClickListener {
            val intentFileManager = Intent(this, DialogFilemanager::class.java)
            someActivityResultLauncher.launch(intentFileManager)
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }

        binding.btnOk.setOnClickListener {
            binding.pbLoadingDialogExported.isVisible = true

            val dir = fileToSave?.absolutePath ?: Util.defaultFolder.absolutePath
            val name = binding.etNameFile.text.toString()
            val rgFormat = binding.rgButtonsFormat.checkedRadioButtonId

            if (name.isNotBlank() && dir.isNotBlank()) {
                val onlyCurrentPage = binding.cbCurrentPage.isChecked

                val bundle = Bundle()
                bundle.putString("file","$dir/$name")
                bundle.putInt("extension",rgFormat)
                bundle.putBoolean("onlyPage",onlyCurrentPage)

                val intent = Intent()
                intent.putExtra("fileExported",bundle)

                setResult(RESULT_CODE_DIALOG_EXPORT,intent)
                finish()
            } else {
                Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT)
                    .show()
            }
            binding.pbLoadingDialogExported.isVisible = false
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