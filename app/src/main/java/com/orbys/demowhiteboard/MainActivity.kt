package com.orbys.demowhiteboard

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.gson.Gson
import com.orbys.demowhiteboard.databinding.ActivityMainBinding
import com.orbys.demowhiteboard.model.MyWhiteboard
import com.orbys.demowhiteboard.ui.DialogImagesBackground
import com.orbys.demowhiteboard.ui.DialogSaveWhiteboard
import com.orbys.demowhiteboard.ui.core.Util
import com.orbys.demowhiteboard.ui.dialog.DialogClose
import com.orbys.demowhiteboard.ui.dialog.DialogExport
import com.orbys.demowhiteboard.ui.dialog.DialogPropsPen
import com.skg.drawaccelerate.AccelerateManager
import java.io.File
import java.io.FileReader
import java.io.FileWriter


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var myWhiteboard: MyWhiteboard

    private var totalPages = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AccelerateManager.instance.onCreate()

        initUI()

        val intentFilter = IntentFilter("action.whiteboard")
        registerReceiver(receiver,intentFilter)
    }

    private val receiver:BroadcastReceiver = object :BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            binding.whiteboard.invalidate()
        }
    }

    private fun initUI() {
        initValues()
        initListenners()
        initListennersMenu()
    }

    private fun initValues() {

        binding.tvCurrentPage.text = GlobalConfig.page.toString()
        binding.tvTotalPage.text = totalPages.toString()

        val files = File(filesDir.absolutePath).listFiles()?.toList()
        if (!files.isNullOrEmpty()) {
            files.forEach {
                it.delete()
            }
        }
    }

    private fun initListenners() {
        /* binding.btnDraw.setOnClickListener {
             GlobalConfig.sMode = 0
         }

         binding.btnEraser.setOnClickListener {
             GlobalConfig.sMode = 1
         }*/

        binding.orbysMenu.setOnClickListener {
            binding.llMenu.isVisible = !binding.llMenu.isVisible
        }

        binding.btnBackground.setOnClickListener {
           Util.initDialogColor(this){ colorSelected ->
               GlobalConfig.backgroundWallpaper = null
               GlobalConfig.backgroundColor = colorSelected

               GlobalConfig.backgroundBitmap = Bitmap.createBitmap(
                   GlobalConfig.SCREEN_WIDTH, GlobalConfig.SCREEN_HEIGHT,
                   Bitmap.Config.ARGB_8888
               ).apply {
                   GlobalConfig.backgroundColor?.let {
                       val canvas = Canvas(this)
                       canvas.drawColor(it)
                   }
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

            val intentDialogImages = Intent(this,DialogImagesBackground::class.java)
            intentDialogImages.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intentDialogImages)

            /*lifecycleScope.launch {
                binding.pbLoading.isVisible = true

                val bitmap: Bitmap = withContext(Dispatchers.IO) {
                    BitmapFactory.decodeResource(
                        this@MainActivity.resources,
                        R.drawable.backgroundimagepaisaje
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
            }*/
        }

        binding.btnClear.setOnClickListener {
            binding.whiteboard.apply {
                clean()
            }
            totalPages = 1
            GlobalConfig.page = 1
            myWhiteboard = MyWhiteboard(mutableListOf())

            binding.tvCurrentPage.text = GlobalConfig.page.toString()
            binding.tvTotalPage.text = totalPages.toString()
        }

        binding.btnPropsPencil.setOnClickListener {
           val dialogPropsPen = DialogPropsPen(this){
               binding.whiteboard.invalidate()
           }
            dialogPropsPen.setCancelable(false)
            dialogPropsPen.show()
        }

        binding.btnSave.setOnClickListener {

            binding.whiteboard.apply {
                saveCall { myLine ->
                    if (::myWhiteboard.isInitialized) {
                        if (myWhiteboard.lines.none { it.page == myLine.page }) {
                            myWhiteboard.lines.add(myLine)
                        } else {
                            myWhiteboard.lines.removeAll { it.page == myLine.page }
                            myWhiteboard.lines.add(myLine)
                        }
                    } else {
                        myWhiteboard = MyWhiteboard(lines = mutableListOf())
                        myWhiteboard.lines.add(myLine)
                    }
                }
            }

            val gson = Gson()
            val json = gson.toJson(myWhiteboard)
            writeJsonToInternalFile(json)

            totalPages = 1
            GlobalConfig.page = 1
            myWhiteboard = MyWhiteboard(mutableListOf())

            binding.tvCurrentPage.text = GlobalConfig.page.toString()
            binding.tvTotalPage.text = totalPages.toString()
        }

        binding.btnOpen.setOnClickListener {
            GlobalConfig.page = 1
            totalPages = 1
            myWhiteboard = MyWhiteboard(lines = mutableListOf())

            val file = File(filesDir, "prueba")

            try {
                // Check if the file exists
                if (file.exists()) {
                    // Read the JSON file
                    val gson = Gson()
                    val fileReader = FileReader(file)
                    val myDataClass = gson.fromJson(fileReader, MyWhiteboard::class.java)
                    binding.whiteboard.drawSavedJson(myDataClass.lines.first { it.page == 1 })
                    myWhiteboard = myDataClass
                    totalPages = myWhiteboard.lines.maxOf { it.page }
                } else {
                    Toast.makeText(this, "No hay archivo guardado", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            binding.tvCurrentPage.text = GlobalConfig.page.toString()
            binding.tvTotalPage.text = totalPages.toString()
        }

        binding.btnRedo.setOnClickListener {
            binding.whiteboard.redoBtn()
        }

        binding.btnUndo.setOnClickListener {
            binding.whiteboard.undoBtn()
        }

        binding.btnExport.setOnClickListener {

            val dir = File(Environment.getExternalStorageDirectory(), "Picture/ORBYS_Whiteboard")
            if (!dir.exists()) {
                val d = dir.mkdirs()
                Log.d("EXPORT", "creado dir $d")
            }

            val dialogExport = DialogExport(this, binding.whiteboard)
            dialogExport.setCancelable(false)
            dialogExport.show()
        }

        binding.btnAddWhiteboard.setOnClickListener {

            if (totalPages == GlobalConfig.LIMIT_PAGES) {
                Toast.makeText(this, "Maximo numero de paginas", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            totalPages++

            binding.whiteboard.apply {
                saveCall { myLine ->
                    if (::myWhiteboard.isInitialized) {
                        if (myWhiteboard.lines.none { it.page == myLine.page }) {
                            myWhiteboard.lines.add(myLine)
                        } else {
                            myWhiteboard.lines.removeAll { it.page == myLine.page }
                            myWhiteboard.lines.add(myLine)
                        }
                    } else {
                        myWhiteboard = MyWhiteboard(lines = mutableListOf())
                        myWhiteboard.lines.add(myLine)
                    }
                }
            }

            GlobalConfig.page = totalPages

            binding.tvCurrentPage.text = GlobalConfig.page.toString()
            binding.tvTotalPage.text = totalPages.toString()
        }

        binding.btnPreviousWhiteboard.setOnClickListener {
            if (GlobalConfig.page > 1) {
                binding.whiteboard.apply {
                    saveCall { myLine ->
                        if (::myWhiteboard.isInitialized) {
                            if (myWhiteboard.lines.none { it.page == myLine.page }) {
                                myWhiteboard.lines.add(myLine)
                            } else {
                                myWhiteboard.lines.removeAll { it.page == myLine.page }
                                myWhiteboard.lines.add(myLine)
                            }
                        } else {
                            myWhiteboard = MyWhiteboard(lines = mutableListOf())
                            myWhiteboard.lines.add(myLine)
                        }
                    }

                    GlobalConfig.page--

                    drawSavedJson(myWhiteboard.lines.first { it.page == GlobalConfig.page })
                }

                binding.tvCurrentPage.text = GlobalConfig.page.toString()
                binding.tvTotalPage.text = totalPages.toString()
            }
        }

        binding.btnLaterWhiteboard.setOnClickListener {
            if (GlobalConfig.page < totalPages) {
                binding.whiteboard.apply {
                    saveCall { myLine ->
                        if (::myWhiteboard.isInitialized) {
                            if (myWhiteboard.lines.none { it.page == myLine.page }) {
                                myWhiteboard.lines.add(myLine)
                            } else {
                                myWhiteboard.lines.removeAll { it.page == myLine.page }
                                myWhiteboard.lines.add(myLine)
                            }
                        } else {
                            myWhiteboard = MyWhiteboard(lines = mutableListOf())
                            myWhiteboard.lines.add(myLine)
                        }
                    }

                    GlobalConfig.page++

                    drawSavedJson(myWhiteboard.lines.first { it.page == GlobalConfig.page })
                }

                binding.tvCurrentPage.text = GlobalConfig.page.toString()
                binding.tvTotalPage.text = totalPages.toString()
            }
        }
    }

    private fun initListennersMenu(){
        binding.tvClose.setOnClickListener {
            //TODO: Dialogo preguntar guardar antes de cerrar

            val dialogClose = DialogClose(this,this)
            dialogClose.setCancelable(false)
            dialogClose.show()
        }

        binding.tvExport.setOnClickListener {
            val dialogExport = DialogExport(this,binding.whiteboard)
            dialogExport.setCancelable(false)
            dialogExport.show()
        }

        binding.tvNew.setOnClickListener {
            //TODO: Dialogo preguntar guardar antes de crear uno nuevo
            binding.whiteboard.apply {
                clean()
            }
            totalPages = 1
            GlobalConfig.page = 1
            myWhiteboard = MyWhiteboard(mutableListOf())

            binding.llMenu.isVisible = false

            Log.d("PAGE", "page ${GlobalConfig.page} total $totalPages")

            binding.tvCurrentPage.text = GlobalConfig.page.toString()
            binding.tvTotalPage.text = totalPages.toString()
        }

        binding.tvSave.setOnClickListener {
            val intentDialogSaveWhiteboard = Intent(this,DialogSaveWhiteboard::class.java)
            intentDialogSaveWhiteboard.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intentDialogSaveWhiteboard)
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

        unregisterReceiver(receiver)
    }

    override fun onBackPressed() {
        super.onBackPressed()

        val dialogClose = DialogClose(this,this)
        dialogClose.setCancelable(false)
        dialogClose.show()
    }
}