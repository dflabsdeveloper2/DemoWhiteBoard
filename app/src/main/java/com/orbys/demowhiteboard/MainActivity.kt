package com.orbys.demowhiteboard

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.orbys.demowhiteboard.databinding.ActivityMainBinding
import com.orbys.demowhiteboard.model.MyLines
import com.orbys.demowhiteboard.ui.core.Util
import com.orbys.demowhiteboard.ui.dialog.DialogPropsPen
import com.skg.drawaccelerate.AccelerateManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileReader
import java.io.FileWriter

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var lines: MyLines
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AccelerateManager.instance.onCreate()

        initUI()
    }

    private fun initUI() {
        checkPermissions()
        initListenners()
    }

    private fun initListenners() {
       /* binding.btnDraw.setOnClickListener {
            GlobalConfig.sMode = 0
        }

        binding.btnEraser.setOnClickListener {
            GlobalConfig.sMode = 1
        }*/

        binding.btnBackground.setOnClickListener {
           Util.initDialogColor(this){ colorSelected ->
               GlobalConfig.backgroundColor = colorSelected
               GlobalConfig.backgroundBitmap = Bitmap.createBitmap(
                   GlobalConfig.SCREEN_WIDTH, GlobalConfig.SCREEN_HEIGHT,
                   Bitmap.Config.ARGB_8888
               ).apply {
                   val canvas = Canvas(this)
                   canvas.drawColor(GlobalConfig.backgroundColor)
               }

               binding.whiteboard.invalidate()
           }
        }

        binding.btnWeb.setOnClickListener {
            val url = "https://myclassbeta.orbys.eu/"

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                Toast.makeText(this,"No hay navegador para abrir la url",Toast.LENGTH_SHORT).show()
            }
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

                GlobalConfig.backgroundBitmap.apply {
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
                GlobalConfig.backgroundBitmap.apply {
                    val canvas = Canvas(this)
                    canvas.drawColor(-0xffa6b0)
                }
                clean()
            }
        }

        binding.btnPropsPencil.setOnClickListener {
           val dialogPropsPen = DialogPropsPen(this){
               binding.whiteboard.invalidate()
           }
            dialogPropsPen.setCancelable(false)
            dialogPropsPen.show()
        }

        binding.btnSave.setOnClickListener {
            //binding.whiteboard.debugCall()
            binding.whiteboard.saveCall {
                lines = it

                val gson = Gson()
                val json = gson.toJson(it)
                writeJsonToInternalFile(json)
            }
        }

        binding.btnOpen.setOnClickListener {
            if (::lines.isInitialized) {
                binding.whiteboard.drawSavedJson(lines)
            } else {
                val file = File(filesDir, "prueba")

                try {
                    // Check if the file exists
                    if (file.exists()) {
                        // Read the JSON file
                        val gson = Gson()
                        val fileReader = FileReader(file)
                        val myDataClass = gson.fromJson(fileReader, MyLines::class.java)
                        binding.whiteboard.drawSavedJson(myDataClass)
                    } else {
                        Toast.makeText(this, "No hay archivo guardado", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        binding.btnRedo.setOnClickListener {
            binding.whiteboard.redoBtn()
        }

        binding.btnUndo.setOnClickListener {
            binding.whiteboard.undoBtn()
        }
    }

    private fun writeJsonToInternalFile(json: String) {
        // Obtenemos el directorio de almacenamiento interno
        val directory = filesDir

        // Creamos el fichero
        val file = File(directory, "prueba")

        // Abrimos el fichero para escritura
        val writer = FileWriter(file)

        // Escribimos el JSON en el fichero
        writer.write(json)

        // Cerramos el fichero
        writer.close()
    }

    private fun checkPermissions(){
        val hasWritePermission = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        if (hasWritePermission) {
            Toast.makeText(this,"PERMISOS CONCEDIDOS",Toast.LENGTH_SHORT).show()
        }
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