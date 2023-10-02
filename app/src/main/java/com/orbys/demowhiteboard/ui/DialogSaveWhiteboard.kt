package com.orbys.demowhiteboard.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.orbys.demowhiteboard.databinding.ActivityDialogSaveWhiteboardBinding

class DialogSaveWhiteboard : AppCompatActivity() {

    private lateinit var binding: ActivityDialogSaveWhiteboardBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDialogSaveWhiteboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI()
    }

    override fun onStart() {
        super.onStart()

        initUI()
    }

    private fun initUI() {
        initValues()
        initListenners()
    }

    private fun initValues() {
        val dir = intent.getStringExtra("directory").orEmpty()
        if(dir.isNotBlank()){
            binding.etFolderFile.setText(dir)
        }else{
            binding.etFolderFile.setText("/Picture/ORBYS_Whiteboard")
        }
    }

    private fun initListenners() {
        binding.etFolderFile.setOnClickListener {
            val intentFileManager = Intent(this,DialogFilemanager::class.java)
            intentFileManager.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intentFileManager)
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }

        binding.btnOk.setOnClickListener {

        }
    }
}