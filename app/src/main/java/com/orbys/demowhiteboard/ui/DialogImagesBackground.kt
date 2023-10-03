package com.orbys.demowhiteboard.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.orbys.demowhiteboard.GlobalConfig
import com.orbys.demowhiteboard.databinding.ActivityDialogImagesBackgroundBinding
import com.orbys.demowhiteboard.ui.adapter.AdapterImagesBackground
import com.orbys.demowhiteboard.ui.core.Util
import java.io.File

class DialogImagesBackground : AppCompatActivity() {

    private lateinit var binding: ActivityDialogImagesBackgroundBinding
    private lateinit var adapterImagesBackround: AdapterImagesBackground

    private val dirName = "ORBYS/Wallpaper"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDialogImagesBackgroundBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI()
    }

    private fun initUI() {
        initValues()
        initRecicler()
        initListenner()
    }

    private fun initValues() {
        val dir = File(Environment.getExternalStorageDirectory(),dirName)
        if(!dir.exists()){
            val a = dir.mkdirs()
            if(!a){
                Log.d("FILE","no se ha creado el dir ORBYS/Wallpaper")
                finish()
            }
        }
    }


    //TODO: loading hasta que se cargen las imagenes
    private fun initRecicler() {
        val files = File(Environment.getExternalStorageDirectory(),dirName).listFiles()?.toList()

        if(!files.isNullOrEmpty()){
            adapterImagesBackround = AdapterImagesBackground(filterImages(files)){
                val bitmap = BitmapFactory.decodeFile(it.path)

                GlobalConfig.backgroundBitmap = Bitmap.createBitmap(GlobalConfig.SCREEN_WIDTH,GlobalConfig.SCREEN_HEIGHT,Bitmap.Config.ARGB_8888).apply {
                    val canvas = Canvas(this)
                    canvas.scale(width.toFloat() / bitmap.width, height.toFloat() / bitmap.height)
                    canvas.drawBitmap(bitmap, 0f, 0f, null)
                }

                /*
                TODO: ajustar al centro manteniendo proporciones imagen
                GlobalConfig.backgroundBitmap.apply {
                    val canvas = Canvas(this)
                    canvas.drawBitmap(bitmap,0f,0f,null)

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
                }*/

                sendBroadcast(Intent("action.whiteboard"))

                GlobalConfig.backgroundWallpaper = Util.createBitmapToBase64String(bitmap)

                finish()
            }

            binding.rvImages.apply {
                layoutManager = GridLayoutManager(this@DialogImagesBackground,4)
                adapter = adapterImagesBackround
            }
        }
    }

    private fun initListenner() {
        binding.ivAddImage.setOnClickListener {

        }
    }

    private fun filterImages(files: List<File>): List<File> {
        return files.filter { it.isFile && it.name.endsWith(".jpg") || it.name.endsWith(".png") }
            .filter { it.length() <= 5000000 }
    }
}