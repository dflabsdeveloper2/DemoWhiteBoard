package com.orbys.demowhiteboard.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import com.orbys.demowhiteboard.databinding.DialogNewFolderBinding

class DialogoNewFolder(context: Context,private var finish:(String)->Unit):Dialog(context) {

    private lateinit var binding: DialogNewFolderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogNewFolderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI()
    }

    private fun initUI() {
        initListenners()
    }

    private fun initListenners() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnCreate.setOnClickListener {
            val name = binding.etNameFile.text.toString()
            if(name.isNotBlank()){
                finish(name)
                dismiss()
            }else{
                Toast.makeText(context,"Rellena el campo",Toast.LENGTH_SHORT).show()
            }
        }
    }
}