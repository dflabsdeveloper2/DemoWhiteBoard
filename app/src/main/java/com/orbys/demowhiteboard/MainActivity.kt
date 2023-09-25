package com.orbys.demowhiteboard

import android.R
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.orbys.demowhiteboard.databinding.ActivityMainBinding
import com.skg.drawaccelerate.AccelerateManager
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AccelerateManager.instance.onCreate()

        initUI()
    }

    private fun initUI() {
        initListenners()
    }

    private fun initListenners() {
        binding.btnDraw.setOnClickListener {
            GlobalConfig.sMode = 0
        }

        binding.btnEraser.setOnClickListener {
            GlobalConfig.sMode = 1
        }

        binding.btnBackground.setOnClickListener {
            GlobalConfig.background = Bitmap.createBitmap(
                GlobalConfig.SCREEN_WIDTH, GlobalConfig.SCREEN_HEIGHT,
                Bitmap.Config.ARGB_8888
            ).apply {
                val canvas = Canvas(this)
                canvas.drawColor(Color.BLUE)
            }

            binding.whiteboard.invalidate()
        }

        binding.btnImageBackground.setOnClickListener {

            lifecycleScope.launch {
                binding.pbLoading.isVisible = true

                val bitmap: Bitmap = withContext(Dispatchers.IO) {
                    BitmapFactory.decodeResource(
                        this@MainActivity.resources,
                        com.orbys.demowhiteboard.R.drawable.backgroundimagepaisaje
                    )
                }

                GlobalConfig.background.apply {
                    val canvas = Canvas(this)

                    val bitmapWidth = bitmap.width
                    val bitmapHeight = bitmap.height
                    val canvasWidth = canvas.width
                    val canvasHeight = canvas.height

                    // Calcula las escalas para ajustar el bitmap al canvas con centerInside
                    val scale = minOf(
                        canvasWidth.toFloat() / bitmapWidth,
                        canvasHeight.toFloat() / bitmapHeight
                    )

                    // Calcula las nuevas dimensiones del bitmap
                    val newBitmapWidth = (bitmapWidth * scale).toInt()
                    val newBitmapHeight = (bitmapHeight * scale).toInt()

                    // Calcula las coordenadas para centrar el bitmap en el canvas
                    val left = (canvasWidth - newBitmapWidth) / 2
                    val top = (canvasHeight - newBitmapHeight) / 2

                    // Crea un rectángulo para el destino
                    val destRect = Rect(left, top, left + newBitmapWidth, top + newBitmapHeight)


                    // Dibuja el bitmap en el canvas con el nuevo tamaño y posición
                    canvas.drawBitmap(bitmap, null, destRect, Paint())
                }

                binding.whiteboard.invalidate()
                binding.pbLoading.isVisible = false
            }
        }

        binding.btnClear.setOnClickListener {
            binding.whiteboard.apply {
                GlobalConfig.background.apply {
                    val canvas = Canvas(this)
                    canvas.drawColor(-0xffa6b0)
                }
                clean()
            }
        }

        binding.btnPalette.setOnClickListener {
            ColorPickerDialog.Builder(this)
                .setTitle("ColorPicker Dialog")
                .setPreferenceName("MyColorPickerDialog")
                .setPositiveButton(getString(R.string.ok),
                    ColorEnvelopeListener { envelope, _ ->
                        GlobalConfig.sPenColor = envelope.color
                        binding.whiteboard.invalidate()
                    })
                .setNegativeButton(
                    getString(R.string.cancel)
                ) { dialogInterface, i -> dialogInterface.dismiss() }
                .attachAlphaSlideBar(true) // the default value is true.
                .attachBrightnessSlideBar(true) // the default value is true.
                .show()
        }

        binding.btnPropsPencil.setOnClickListener {
            initSeekBar()
        }
    }

    private fun initSeekBar(){
        val props =binding.sbPropsPen
        props.min = 0
        props.max = 100
        props.progress = GlobalConfig.sPenWidth.toInt()

        props.isVisible = true

        props.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                GlobalConfig.sPenWidth = progress.toFloat()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Hacer algo al comenzar a arrastrar el control deslizante
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // Hacer algo al terminar de arrastrar el control deslizante
                props.isVisible = false
                binding.whiteboard.invalidate()
            }
        })
    }
    override fun onStart() {
        super.onStart()
        AccelerateManager.instance.onStart()
    }

    override fun onResume() {
        super.onResume()
        AccelerateManager.instance.onResume()
    }

    override fun onPause() {
        super.onPause()
        AccelerateManager.instance.onPause()
    }

    override fun onStop() {
        super.onStop()
        AccelerateManager.instance.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        AccelerateManager.instance.onDestroy()
    }
}