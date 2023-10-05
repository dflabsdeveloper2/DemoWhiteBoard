package com.orbys.demowhiteboard

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.orbys.demowhiteboard.databinding.ActivityMainBinding
import com.orbys.demowhiteboard.domain.BitmapWhiteboard
import com.orbys.demowhiteboard.model.MyWhiteboard
import com.orbys.demowhiteboard.ui.DialogExport
import com.orbys.demowhiteboard.ui.DialogFilemanager
import com.orbys.demowhiteboard.ui.DialogImagesBackground
import com.orbys.demowhiteboard.ui.DialogSaveWhiteboard
import com.orbys.demowhiteboard.ui.core.Helper
import com.orbys.demowhiteboard.ui.core.Util
import com.orbys.demowhiteboard.ui.dialog.DialogClose
import com.orbys.demowhiteboard.ui.dialog.DialogNewWhiteboard
import com.orbys.demowhiteboard.ui.dialog.DialogPropsPen
import com.skg.drawaccelerate.AccelerateManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var myWhiteboard: MyWhiteboard
    private lateinit var someActivityResultLauncher: ActivityResultLauncher<Intent>

    private val myBitmapsFromWhiteboard = mutableMapOf<Int, Bitmap>()


    private var totalPages = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AccelerateManager.instance.onCreate()

        if (checkPermission()) {
            initUI()
        } else {
            requestPermission()
        }
    }

    private fun checkPermission(): Boolean = Environment.isExternalStorageManager()

    private fun requestPermission() {
        Log.d("PERMISSION", "request permission")

        Toast.makeText(this, "Son necesarios los permisos", Toast.LENGTH_SHORT).show()
        try {
            val intent = Intent()
            intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
            val uri = Uri.fromParts("package", this.packageName, null)
            intent.data = uri
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            val intent = Intent()
            intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
            startActivity(intent)
            finish()
        }
    }

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            binding.whiteboard.invalidate()
        }
    }

    private fun initUI() {
        initValues()
        initListenners()
        initListennersMenu()
        getDataResultActivities()
        registerLocalBroadcast()
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
            val url = com.orbys.demowhiteboard.core.Util.urlMyClass

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                Toast.makeText(this,"No hay navegador para abrir la url",Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnImageBackground.setOnClickListener {
            val intentDialogImages = Intent(this,DialogImagesBackground::class.java)
            someActivityResultLauncher.launch(intentDialogImages)
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
            val intentSave = Intent(this, DialogSaveWhiteboard::class.java)
            someActivityResultLauncher.launch(intentSave)
        }

        binding.btnOpen.setOnClickListener {
            val intentOpen = Intent(this,DialogFilemanager::class.java)
            intentOpen.putExtra("open",true)
            someActivityResultLauncher.launch(intentOpen)
        }

        binding.btnRedo.setOnClickListener {
            binding.whiteboard.redoBtn()
        }

        binding.btnUndo.setOnClickListener {
            binding.whiteboard.undoBtn()
        }

        binding.btnExport.setOnClickListener {
            binding.pbLoading.isVisible = true

            val bitmap = BitmapWhiteboard.getBitmapWhiteBoard(binding.whiteboard)
            myBitmapsFromWhiteboard[GlobalConfig.page] = bitmap

            val intentDialogExport = Intent(this, DialogExport::class.java)
            someActivityResultLauncher.launch(intentDialogExport)
            /*val dir = File(Environment.getExternalStorageDirectory(), "Picture/ORBYS_Whiteboard")
            if (!dir.exists()) {
                val d = dir.mkdirs()
                Log.d("EXPORT", "creado dir $d")
            }

            val bitmap = BitmapWhiteboard.getBitmapWhiteBoard(binding.whiteboard)
            myBitmapsFromWhiteboard[GlobalConfig.page] = bitmap

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    myBitmapsFromWhiteboard.forEach { (page, whiteboard) ->
                        Log.d("BITMAP", "page: $page whiteboard ${whiteboard.byteCount}")

                        val fileExported =
                            File(
                                Environment.getExternalStorageDirectory(),
                                "ORBYS/prueba-$page.png"
                            )
                        val fileOutputStream = FileOutputStream(fileExported)

                        whiteboard.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
                        fileOutputStream.close()
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MainActivity,
                            "Imagen guardada en ORBYS",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MainActivity,
                            "Error al exportar la imagen",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
            }*/
            binding.pbLoading.isVisible = false
        }

        binding.btnAddWhiteboard.setOnClickListener {
            binding.pbLoading.isVisible = true
            if (totalPages == GlobalConfig.LIMIT_PAGES) {
                Toast.makeText(this, "Maximo numero de paginas", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            totalPages++

            binding.whiteboard.apply {

                val bitmap = BitmapWhiteboard.getBitmapWhiteBoard(this)
                myBitmapsFromWhiteboard[GlobalConfig.page] = bitmap

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
            binding.pbLoading.isVisible = false
        }

        binding.btnPreviousWhiteboard.setOnClickListener {
            binding.pbLoading.isVisible = true
            if (GlobalConfig.page > 1) {
                binding.whiteboard.apply {

                    val bitmap = BitmapWhiteboard.getBitmapWhiteBoard(this)
                    myBitmapsFromWhiteboard[GlobalConfig.page] = bitmap

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
            binding.pbLoading.isVisible = false
        }

        binding.btnLaterWhiteboard.setOnClickListener {
            binding.pbLoading.isVisible = true
            if (GlobalConfig.page < totalPages) {
                binding.whiteboard.apply {

                    val bitmap = BitmapWhiteboard.getBitmapWhiteBoard(this)
                    myBitmapsFromWhiteboard[GlobalConfig.page] = bitmap

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
            binding.pbLoading.isVisible = false
        }
    }

    private fun initListennersMenu(){
        binding.tvClose.setOnClickListener {
            //TODO: Dialogo preguntar guardar antes de cerrar

            val dialogClose = DialogClose(this){
                finish()
            }
            dialogClose.setCancelable(false)
            dialogClose.show()
        }

        binding.tvExport.setOnClickListener {
            val intentDialogExport = Intent(this, DialogExport::class.java)
            someActivityResultLauncher.launch(intentDialogExport)

            binding.llMenu.isVisible = false
        }

        binding.tvNew.setOnClickListener {
            //TODO: Dialogo preguntar guardar antes de crear uno nuevo
            val dialogNew = DialogNewWhiteboard(this) {
                //true -> new  false -> save whiteboard
                if (it) {
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
                } else {
                    val intentSave = Intent(this, DialogSaveWhiteboard::class.java)
                    someActivityResultLauncher.launch(intentSave)

                    binding.llMenu.isVisible = false
                }
            }

            dialogNew.show()
        }

        binding.tvSave.setOnClickListener {
            val intentSave = Intent(this, DialogSaveWhiteboard::class.java)
            someActivityResultLauncher.launch(intentSave)

            binding.llMenu.isVisible = false
        }

        binding.tvOpen.setOnClickListener {
            val intentOpen = Intent(this,DialogFilemanager::class.java)
            intentOpen.putExtra("open",true)
            someActivityResultLauncher.launch(intentOpen)

            binding.llMenu.isVisible = false
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

        unregisterReceiver(receiver)
    }

    override fun onBackPressed() {
        super.onBackPressed()

        val dialogClose = DialogClose(this){
            finish()
        }
        dialogClose.setCancelable(false)
        dialogClose.show()
    }

    private fun registerLocalBroadcast() {
        val intentFilter = IntentFilter("action.whiteboard")
        registerReceiver(receiver, intentFilter)
    }

    private fun getDataResultActivities() {
        someActivityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                when (result.resultCode) {

                    DialogImagesBackground.RESULT_CODE_DIALOG_WALLPAPER -> {
                        // El código de resultado está OK, puedes procesar los datos recibidos aquí
                        val data: Intent? = result.data
                        val dataString = data?.getStringExtra("fileWallpaper").orEmpty()
                        if (dataString.isEmpty()) return@registerForActivityResult

                        binding.pbLoading.isVisible = true

                        lifecycleScope.launch(Dispatchers.IO) {
                            val bitmap = BitmapFactory.decodeFile(dataString)
                            GlobalConfig.backgroundBitmap = Bitmap.createBitmap(
                                GlobalConfig.SCREEN_WIDTH,
                                GlobalConfig.SCREEN_HEIGHT,
                                Bitmap.Config.ARGB_8888
                            ).apply {
                                val canvas = Canvas(this)
                                canvas.scale(
                                    width.toFloat() / bitmap.width,
                                    height.toFloat() / bitmap.height
                                )
                                canvas.drawBitmap(bitmap, 0f, 0f, null)
                            }

                            GlobalConfig.backgroundWallpaper =
                                Helper.createBitmapToBase64String(bitmap)
                            //sendBroadcast(Intent("action.whiteboard"))

                            withContext(Dispatchers.Main) {
                                binding.whiteboard.invalidate()
                                binding.pbLoading.isVisible = false
                            }
                        }
                    }

                    DialogSaveWhiteboard.RESULT_CODE_DIALOG_SAVE -> {
                        val data: Intent? = result.data
                        val dataString = data?.getStringExtra("fileSave").orEmpty()

                        if (dataString.isNotBlank()) {
                            val file = File(dataString)
                            Log.d("SAVE", "file: ${file.absolutePath}")
                            binding.pbLoading.isVisible = true
                            binding.whiteboard.saveCall { myLine ->
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

                            val gson = Gson()
                            val json = gson.toJson(myWhiteboard)
                            Helper.writeJsonToInternalFile(json, file)

                            totalPages = 1
                            GlobalConfig.page = 1
                            myWhiteboard = MyWhiteboard(mutableListOf())


                            binding.tvCurrentPage.text = GlobalConfig.page.toString()
                            binding.tvTotalPage.text = totalPages.toString()
                            binding.pbLoading.isVisible = false
                        }
                    }

                    DialogFilemanager.RESULT_CODE_DIALOG_FILEMANAGER_OPEN_FILE -> {
                        val data: Intent? = result.data
                        val dataString = data?.getStringExtra("fileOpen").orEmpty()

                        GlobalConfig.page = 1
                        totalPages = 1
                        myWhiteboard = MyWhiteboard(lines = mutableListOf())

                        val file = File(dataString)

                        try {
                            // Check if the file exists
                            if (file.exists()) {
                                // Read the JSON file
                                val gson = Gson()
                                val fileReader = FileReader(file)
                                val myDataClass =
                                    gson.fromJson(fileReader, MyWhiteboard::class.java)
                                binding.whiteboard.drawSavedJson(myDataClass.lines.first { it.page == 1 })
                                myWhiteboard = myDataClass
                                totalPages = myWhiteboard.lines.maxOf { it.page }
                            } else {
                                Toast.makeText(this, "No hay archivo guardado", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        binding.tvCurrentPage.text = GlobalConfig.page.toString()
                        binding.tvTotalPage.text = totalPages.toString()
                    }

                    DialogExport.RESULT_CODE_DIALOG_EXPORT -> {
                        binding.pbLoading.isVisible = true
                        val data: Intent? = result.data
                        val dataBundle = data?.getBundleExtra("fileExported")
                        val fileExported = dataBundle?.getString("file")
                        val rgFormat = dataBundle?.getInt("extension")
                        val onlyCurrentPage = dataBundle?.getBoolean("onlyPage")

                        if (rgFormat != null && fileExported != null && onlyCurrentPage != null) {
                            val extension: String
                            val format: Bitmap.CompressFormat? = when (rgFormat) {
                                R.id.rbJPEG -> {
                                    extension = ".jpeg"
                                    Bitmap.CompressFormat.JPEG
                                }

                                R.id.rbPNG -> {
                                    extension = ".png"
                                    Bitmap.CompressFormat.PNG
                                }

                                else -> {
                                    extension = ".pdf"
                                    null
                                }
                            }

                            try {
                                if (format == null) {
                                    if (onlyCurrentPage) {
                                        val bitmap = myBitmapsFromWhiteboard[GlobalConfig.page]
                                        bitmap?.let {
                                            Helper.createPdfWithBitmaps(
                                                listOf(it),
                                                File(
                                                    fileExported + extension
                                                )
                                            )
                                        }
                                    } else {
                                        Helper.createPdfWithBitmaps(
                                            myBitmapsFromWhiteboard.map { it.value }, File(
                                                fileExported + extension
                                            )
                                        )
                                    }
                                } else {
                                    myBitmapsFromWhiteboard.forEach { (page, whiteboard) ->
                                        if (onlyCurrentPage) {
                                            if (GlobalConfig.page == page) {
                                                val file = File(
                                                    fileExported + extension
                                                )

                                                val fileOutputStream = FileOutputStream(file)
                                                whiteboard.compress(
                                                    format,
                                                    100,
                                                    fileOutputStream
                                                )
                                                fileOutputStream.close()

                                                return@forEach
                                            }
                                        } else {
                                            val file = File(
                                                "$fileExported-$page$extension"
                                            )

                                            Log.d("EXPORT", "file ${file.path}")

                                            val fileOutputStream = FileOutputStream(file)
                                            whiteboard.compress(format, 100, fileOutputStream)
                                            fileOutputStream.close()
                                        }
                                    }
                                }

                                Toast.makeText(
                                    this@MainActivity,
                                    "Imagen guardada en $fileExported",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            } catch (e: Exception) {

                                Toast.makeText(
                                    this@MainActivity,
                                    "Error al guardar la imagen",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        }

                        binding.pbLoading.isVisible = false
                    }
                }
            }
    }
}