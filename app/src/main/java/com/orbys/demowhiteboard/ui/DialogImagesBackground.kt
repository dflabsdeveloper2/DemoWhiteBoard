package com.orbys.demowhiteboard.ui

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.orbys.demowhiteboard.databinding.ActivityDialogImagesBackgroundBinding
import com.orbys.demowhiteboard.ui.adapter.AdapterImagesBackground
import java.io.File

class DialogImagesBackground : AppCompatActivity() {

    private lateinit var binding: ActivityDialogImagesBackgroundBinding
    private lateinit var adapterImagesBackround: AdapterImagesBackground

    private val dirName = "ORBYS/Wallpaper"

    companion object {
        const val RESULT_CODE_DIALOG_WALLPAPER = 123 // Código de resultado personalizado
    }

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
                val intentData = Intent()
                intentData.putExtra("fileWallpaper", it.path.toString())
                setResult(RESULT_CODE_DIALOG_WALLPAPER,intentData)
                Log.d("IMAGE","finish")
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
            val intentOpen = Intent(this,DialogFilemanager::class.java)
            intentOpen.putExtra("addImage",true)
            someActivityResultLauncher.launch(intentOpen)
        }
    }

    private fun filterImages(files: List<File>): List<File> {
        return files.filter { it.isFile && it.name.endsWith(".jpg") || it.name.endsWith(".png") }
            .filter { it.length() <= 5000000 }
    }

    private val someActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            //Dialog Wallpaper
            if (result.resultCode == DialogFilemanager.RESULT_CODE_DIALOG_FILEMANAGER_ADD_IMAGE) {
                val data: Intent? = result.data
                val dataString = data?.getStringExtra("image").orEmpty()
                if (dataString.isNotBlank()) {
                    val intentData = Intent()
                    intentData.putExtra("fileWallpaper", dataString)
                    setResult(RESULT_CODE_DIALOG_WALLPAPER,intentData)
                    Log.d("IMAGE","finish")
                    finish()
                }
            }
        }
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