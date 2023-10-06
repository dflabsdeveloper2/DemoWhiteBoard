package com.orbys.demowhiteboard.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Bundle
import android.widget.SeekBar
import com.orbys.demowhiteboard.core.GlobalConfig
import com.orbys.demowhiteboard.databinding.DialogStylePenBinding
import com.orbys.demowhiteboard.ui.core.Util

class DialogPropsPen(context: Context,private val finish:()->Unit) : Dialog(context) {

    private lateinit var binding: DialogStylePenBinding

    private var widthPenFino: Float = 0.0f
    private var widthPenGrueso: Float = 0.0f

    private lateinit var roundedViewColorFino:ShapeDrawable
    private lateinit var roundedViewColorGrueso:ShapeDrawable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogStylePenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI()
    }

    private fun initUI() {
        initValues()
        initListenners()
    }

    private fun initListenners() {

        binding.sbPropsPenFino.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                widthPenFino = progress.toFloat()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Hacer algo al comenzar a arrastrar el control deslizante
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // Hacer algo al terminar de arrastrar el control deslizante
            }
        })

        binding.sbPropsPenGrueso.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                widthPenGrueso = progress.toFloat()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Hacer algo al comenzar a arrastrar el control deslizante
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // Hacer algo al terminar de arrastrar el control deslizante
            }
        })

        binding.vColorFino.setOnClickListener {
            Util.initDialogColor(it.context) { colorSelected ->
                roundedViewColorFino.paint.color = colorSelected
                binding.vColorFino.background = roundedViewColorFino
                binding.vColorFino.invalidate()
            }
        }

        binding.vColorGrueso.setOnClickListener {
            Util.initDialogColor(it.context) { colorSelected ->
                roundedViewColorGrueso.paint.color = colorSelected
                binding.vColorGrueso.background = roundedViewColorGrueso
                binding.vColorGrueso.invalidate()
            }
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnConfirm.setOnClickListener {
            //GUARDAR DATOS

            GlobalConfig.penWidthFino = widthPenFino
            GlobalConfig.penWidthGrueso = widthPenGrueso
            GlobalConfig.penColorFino = roundedViewColorFino.paint.color
            GlobalConfig.penColorGrueso = roundedViewColorGrueso.paint.color

            finish()
            dismiss()
        }
    }

    private fun initValues() {
        //View redonda donde aparece el color que estas pintando
        roundedViewColorFino = ShapeDrawable(OvalShape())
        roundedViewColorGrueso = ShapeDrawable(OvalShape())

        roundedViewColorFino.paint.color = GlobalConfig.penColorFino
        roundedViewColorGrueso.paint.color = GlobalConfig.penColorGrueso

        binding.vColorFino.background = roundedViewColorFino
        binding.vColorGrueso.background = roundedViewColorGrueso

        widthPenFino = GlobalConfig.penWidthFino
        widthPenGrueso = GlobalConfig.penWidthGrueso

        binding.sbPropsPenFino.progress = widthPenFino.toInt()
        binding.sbPropsPenGrueso.progress = widthPenGrueso.toInt()
    }
}