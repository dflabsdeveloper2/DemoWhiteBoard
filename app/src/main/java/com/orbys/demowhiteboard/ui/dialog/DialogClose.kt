package com.orbys.demowhiteboard.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.orbys.demowhiteboard.databinding.DialogCloseBinding

class DialogClose(context: Context,private val activity: Activity):Dialog(context) {

    private lateinit var binding: DialogCloseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogCloseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnClose.setOnClickListener {
            activity.finish()
        }
    }
}