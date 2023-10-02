package com.orbys.demowhiteboard.ui

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.orbys.demowhiteboard.databinding.ActivityDialogFilemanagerBinding
import com.orbys.demowhiteboard.ui.adapter.AdapterFiles
import java.io.File

class DialogFilemanager : AppCompatActivity() {

    private lateinit var binding: ActivityDialogFilemanagerBinding

    private lateinit var adapterFiles: AdapterFiles
    private lateinit var currentPath: File


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
        currentPath = File(Environment.getExternalStorageDirectory().absolutePath)

        binding.tvPath.text = currentPath.path
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
            val dir = File(currentPath.absolutePath,"nuevo")
            val a = dir.mkdirs()
            if(a){
                if(dir.exists()){
                    currentPath = dir
                }
            }
        }

        binding.btnSelect.setOnClickListener {
            val intentSaveWhiteboard = Intent(this,DialogSaveWhiteboard::class.java)
            intentSaveWhiteboard.putExtra("directory",currentPath.absolutePath)
            startActivity(intentSaveWhiteboard)
            finish()
        }
    }
}