package com.orbys.demowhiteboard.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.orbys.demowhiteboard.databinding.DialogNewWhiteboardBinding

class DialogNewWhiteboard(context: Context,private val finish:(Boolean)->Unit):Dialog(context) {

    private lateinit var binding: DialogNewWhiteboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogNewWhiteboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI()
    }

    private fun initUI() {
        initListenners()
    }

    private fun initListenners() {
        binding.btnNew.setOnClickListener {
            finish(true)
            dismiss()
        }
        binding.btnSave.setOnClickListener {
            finish(false)
            dismiss()
        }
    }
}