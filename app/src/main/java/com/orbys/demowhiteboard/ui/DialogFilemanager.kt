package com.orbys.demowhiteboard.ui

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.orbys.demowhiteboard.databinding.ActivityDialogFilemanagerBinding
import com.orbys.demowhiteboard.ui.adapter.AdapterFiles
import com.orbys.demowhiteboard.ui.dialog.DialogoNewFolder
import java.io.File

class DialogFilemanager : AppCompatActivity() {

    private lateinit var binding: ActivityDialogFilemanagerBinding

    private lateinit var adapterFiles: AdapterFiles
    private lateinit var currentPath: File

    private var openFileMode:Boolean = false
    private var addImageMode:Boolean = false

    companion object {
        const val RESULT_CODE_DIALOG_FILEMANAGER = 321 // Código de resultado personalizado
        const val RESULT_CODE_DIALOG_FILEMANAGER_OPEN_FILE = 7889
        const val RESULT_CODE_DIALOG_FILEMANAGER_ADD_IMAGE = 87213
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDialogFilemanagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI()
    }

    private fun initUI() {
        initValues()
        initReciclerView()
        initListenners()
    }

    private fun initValues() {
        openFileMode = intent.getBooleanExtra("open", false)
        addImageMode = intent.getBooleanExtra("addImage",false)

        currentPath = File(Environment.getExternalStorageDirectory().absolutePath)

        binding.tvPath.text = currentPath.path

        binding.btnCreateNewFolder.isVisible = !openFileMode && !addImageMode
        binding.btnSelect.isVisible = !openFileMode && !addImageMode
    }

    private fun initReciclerView() {
        if (!currentPath.listFiles()?.toList().isNullOrEmpty()) {
            adapterFiles = AdapterFiles(currentPath.listFiles()!!.toList()) {
                onClickAdapter(it)
            }

            binding.rvFiles.apply {
                layoutManager = GridLayoutManager(this@DialogFilemanager, 3)
                adapter = adapterFiles
            }
        }
    }

    private fun onClickAdapter(position: Int) {
        val list = currentPath.listFiles()?.toList()
        if (!list.isNullOrEmpty()) {
            currentPath = list[position]
            if (openFileMode) {
                if (currentPath.isFile) {
                    val intentSaveWhiteboard = Intent()
                    intentSaveWhiteboard.putExtra("fileOpen", currentPath.absolutePath)
                    setResult(RESULT_CODE_DIALOG_FILEMANAGER_OPEN_FILE, intentSaveWhiteboard)
                    finish()
                }
            }

            if(addImageMode){
                if(currentPath.isFile){
                    if(currentPath.name.endsWith(".png") || currentPath.name.endsWith(".jpg")){
                        val intentSaveWhiteboard = Intent()
                        intentSaveWhiteboard.putExtra("image", currentPath.absolutePath)
                        setResult(RESULT_CODE_DIALOG_FILEMANAGER_ADD_IMAGE, intentSaveWhiteboard)
                        finish()
                    }else{
                        Toast.makeText(this,"Formato no soportado en la pizarra",Toast.LENGTH_SHORT).show()
                    }
                }
            }
            currentPath.listFiles()?.let { adapterFiles.updateList(it.toList()) }
        }
    }

    private fun initListenners() {

        binding.ivBack.setOnClickListener {
            currentPath =
                if (currentPath.path != Environment.getExternalStorageDirectory().toString()) {
                    currentPath.parentFile
                } else {
                    currentPath
                }
        }

        binding.ivClose.setOnClickListener {
            finish()
        }

        binding.btnCreateNewFolder.setOnClickListener {

            val dialogNewFolder = DialogoNewFolder(this){
                val dir = File(currentPath.absolutePath,it)
                val a = dir.mkdirs()
                if(a){
                    if(dir.exists()){
                        currentPath = dir
                    }
                }
            }
            dialogNewFolder.setCancelable(false)
            dialogNewFolder.show()
        }

        binding.btnSelect.setOnClickListener {
            val intentSaveWhiteboard = Intent()
            intentSaveWhiteboard.putExtra("directory",currentPath.absolutePath)
            setResult(RESULT_CODE_DIALOG_FILEMANAGER,intentSaveWhiteboard)
            finish()
        }
    }
}