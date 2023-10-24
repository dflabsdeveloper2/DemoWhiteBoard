package com.orbys.demowhiteboard.ui.whiteboard

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import android.widget.Toast
import com.orbys.demowhiteboard.core.GlobalConfig
import com.orbys.demowhiteboard.domain.drawline.AccelerateDrawLineActor
import com.orbys.demowhiteboard.domain.drawline.DrawLineActor
import com.orbys.demowhiteboard.domain.drawline.LineData
import com.orbys.demowhiteboard.domain.eraser.AccelerateEraserActor
import com.orbys.demowhiteboard.domain.eraser.EraseData
import com.orbys.demowhiteboard.domain.eraser.EraserActor
import com.orbys.demowhiteboard.domain.model.ImageBitmap
import com.orbys.demowhiteboard.domain.model.ImageBitmap2
import com.orbys.demowhiteboard.domain.model.MyLine
import com.orbys.demowhiteboard.domain.model.MyLines
import com.orbys.demowhiteboard.domain.model.MyPaint
import com.orbys.demowhiteboard.ui.core.Helper
import com.skg.drawaccelerate.AccelerateManager

class WriteBoardController(private val context:Context, private val callBack: () -> Unit) : Handler.Callback {
    private var mDisplayBitmap: Bitmap? = null
    private var mDisplayCanvas: Canvas? = null
    private var mBufferBitmap: Bitmap? = null
    private var mBufferCanvas: Canvas? = null
    private val mHandlerThread: HandlerThread = HandlerThread("temp-r")
    private val mHandler: Handler

    // Imagen seleccionada para mover
    private var selectedImage: ImageBitmap2? = null

    //private val mEraserIndicatorPaint = Paint()
    private val mEraserPaint = MyPaint(ereaser = true)
    private var myLines: MutableList<MyLine>
    private var myUndoLines: MutableList<MyLine>
    private var myLinesHistory: MutableList<MyLine>

    init {
        mHandlerThread.start()
        mHandler = Handler(mHandlerThread.looper, this)
        //mEraserIndicatorPaint.color = Color.WHITE
        myLines = mutableListOf()
        myLinesHistory = mutableListOf()
        myUndoLines = mutableListOf()
    }

    fun drawLineAccelerate(data: LineData?) {
        mHandler.obtainMessage(WriteCommand.DRAW_LINE_ACCELERATE, data).sendToTarget()
    }

    fun eraseAccelerate(data: EraseData?) {
        mHandler.obtainMessage(WriteCommand.ERASER_ACCELERATE, data).sendToTarget()
    }

    fun debug() {
        mHandler.obtainMessage(WriteCommand.DEBUG_LINE).sendToTarget()
    }

    fun drawSaved(data: MyLines?) {
        mHandler.obtainMessage(WriteCommand.DRAW_SAVED, data).sendToTarget()
    }

    fun redoAction() {
        mHandler.obtainMessage(WriteCommand.REDO).sendToTarget()
    }

    fun undoAction() {
        mHandler.obtainMessage(WriteCommand.UNDO).sendToTarget()
    }

    fun moveBitmap(data: Pair<ImageBitmap2, String>) {
        mHandler.obtainMessage(WriteCommand.MOVE_BITMAP, data).sendToTarget()
    }

    fun addImageBitmap(data: ImageBitmap) {
        mHandler.obtainMessage(WriteCommand.DRAW_BITMAP, data).sendToTarget()
    }

    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            WriteCommand.DEBUG_LINE -> {
                myLines.forEach {
                    Log.d("LINES", "linia: $it")
                }
            }

            WriteCommand.REDO -> {
                Log.d("REDO", "REDO")
                if (myUndoLines.isNotEmpty()) {
                    val lastRedoneLine = myUndoLines.removeAt(myUndoLines.size - 1)
                    myLines.add(lastRedoneLine)

                    clearToRender()

                    myLines.forEach { line ->
                        if (line.props != null) {
                            if (!line.props.ereaser) {
                                val lineData = LineData(line.props.color, line.props.strokeWidth)
                                line.line!!.forEach { point ->
                                    lineData.addPoint(point.x, point.y)
                                }
                                mBufferCanvas?.drawPath(lineData.toPath(), line.props.toPaint())
                            } else {
                                line.lineEraser!!.forEach { rect ->
                                    if (rect != null) {
                                        mBufferCanvas?.drawRect(rect, line.props.toPaint())
                                    }
                                }
                            }
                        } else {
                            if (line.imageBitmap != null) {
                                val dstRect = RectF(
                                    line.imageBitmap!!.x,
                                    line.imageBitmap!!.y,
                                    line.imageBitmap!!.x + line.imageBitmap!!.width,
                                    line.imageBitmap!!.y + line.imageBitmap!!.height
                                )

                                mBufferCanvas?.drawBitmap(
                                    line.imageBitmap!!.image,
                                    null,
                                    dstRect,
                                    null
                                )
                            }
                        }
                    }

                    render()
                } else {
                    Toast.makeText(context, "No hay más registros para rehacer", Toast.LENGTH_SHORT).show()
                }
            }

            WriteCommand.UNDO -> {
                Log.d("UNDO", "UNDO")
                if (myLines.isNotEmpty()) {
                    val lastUndoneLine = myLines.removeAt(myLines.size - 1)
                    myUndoLines.add(lastUndoneLine)

                    clearToRender()

                    myLines.forEach { line ->
                        if (line.props != null) {
                            if (!line.props.ereaser) {
                                val lineData = LineData(line.props.color, line.props.strokeWidth)
                                line.line!!.forEach { point ->
                                    lineData.addPoint(point.x, point.y)
                                }
                                mBufferCanvas?.drawPath(lineData.toPath(), line.props.toPaint())
                            } else {
                                line.lineEraser!!.forEach { rect ->
                                    if (rect != null) {
                                        mBufferCanvas?.drawRect(rect, line.props.toPaint())
                                    }
                                }
                            }
                        } else {
                            if (line.imageBitmap != null) {
                                val dstRect = RectF(
                                    line.imageBitmap!!.x,
                                    line.imageBitmap!!.y,
                                    line.imageBitmap!!.x + line.imageBitmap!!.width,
                                    line.imageBitmap!!.y + line.imageBitmap!!.height
                                )

                                mBufferCanvas?.drawBitmap(
                                    line.imageBitmap!!.image,
                                    null,
                                    dstRect,
                                    null
                                )
                            }
                        }
                    }

                    render()
                } else {
                    Toast.makeText(context, "No hay más registros para deshacer", Toast.LENGTH_SHORT).show()
                }
            }

            WriteCommand.DRAW_SAVED -> {
                clear()

                Log.d("SAVE", "OPEN saved whiteboard")
                val obj = msg.obj as? MyLines ?: return true

                GlobalConfig.backgroundWallpaper = obj.backgroundWallpaper
                GlobalConfig.backgroundColor = obj.backgroundColor
                GlobalConfig.currentPage = obj.page

                val lineDraw = obj.listLines

                val bitmapWallpaper =
                    GlobalConfig.backgroundWallpaper?.let { Helper.createBitmapFromBase64String(it) }


                //establecer bitmap wallpaper
                GlobalConfig.backgroundBitmap = Bitmap.createBitmap(
                    GlobalConfig.SCREEN_WIDTH, GlobalConfig.SCREEN_HEIGHT,
                    Bitmap.Config.ARGB_8888
                ).apply {
                    val canvas = Canvas(this)
                    if (bitmapWallpaper != null) {
                        canvas.scale(
                            width.toFloat() / bitmapWallpaper.width,
                            height.toFloat() / bitmapWallpaper.height
                        )
                        canvas.drawBitmap(bitmapWallpaper, 0f, 0f, null)
                    } else {

                        if (GlobalConfig.backgroundColor != null) {
                            canvas.drawColor(GlobalConfig.backgroundColor!!)
                        } else {
                            canvas.drawColor(GlobalConfig.defaultBackgroundColor)
                        }
                    }
                }

                val offscreenBitmap = Bitmap.createBitmap(
                    GlobalConfig.SCREEN_WIDTH,
                    GlobalConfig.SCREEN_HEIGHT,
                    Bitmap.Config.ARGB_8888
                )
                val offscreenCanvas = Canvas(offscreenBitmap)

                lineDraw.forEach {
                    if (it.props != null) {
                        if (!it.props.ereaser) {
                            val lineData = LineData(it.props.color, it.props.strokeWidth)
                            it.line!!.forEach { point ->
                                lineData.addPoint(point.x, point.y)
                            }
                            offscreenCanvas.drawPath(lineData.toPath(), it.props.toPaint())
                        } else {
                            it.lineEraser!!.forEach { rect ->
                                if (rect != null) {
                                    offscreenCanvas.drawRect(rect, it.props.toPaint())
                                }
                            }
                        }
                    } else {
                        if (it.imageBitmap != null) {
                            val dstRect = RectF(
                                it.imageBitmap!!.x,
                                it.imageBitmap!!.y,
                                it.imageBitmap!!.x + it.imageBitmap!!.width,
                                it.imageBitmap!!.y + it.imageBitmap!!.height
                            )

                            offscreenCanvas.drawBitmap(it.imageBitmap!!.image, null, dstRect, null)
                        }
                    }
                }

                mBufferCanvas?.drawBitmap(offscreenBitmap, 0f, 0f, null)

                render()

                myLines = lineDraw.toMutableList()
            }

            WriteCommand.CLEAN -> {
                //mStrokesCanvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                clear()
            }

            WriteCommand.MOVE_BITMAP -> {
                val obj = msg.obj as? Pair<ImageBitmap2, String> ?: return true
                selectedImage = obj.first

                selectedImage?.let { image ->
                    // Crear una copia del búfer de dibujo original sin el bitmap seleccionado
                    val tempBufferBitmap = mBufferBitmap?.copy(Bitmap.Config.ARGB_8888, true)
                    val tempBufferCanvas = Canvas(tempBufferBitmap!!)

                    // Dibujar el bitmap seleccionado en su nueva posición en la copia del búfer de dibujo
                    val dstRect =
                        RectF(image.x, image.y, image.x + image.width, image.y + image.height)
                    tempBufferCanvas.drawBitmap(image.image, null, dstRect, null)

                    // Asignar la copia del búfer de dibujo al búfer de dibujo actual
                    mBufferBitmap?.recycle()
                    mBufferBitmap = tempBufferBitmap

                    // Renderizar el canvas
                    render()

                    if (obj.second == "finish") {
                        myLines.removeIf { it.imageBitmap?.image == selectedImage?.image }
                        myLines.add(MyLine(null, null, null, selectedImage))
                        selectedImage = null
                    }
                }
            }

            WriteCommand.DRAW_BITMAP -> {
                val obj = msg.obj as? ImageBitmap ?: return true
                val data: ImageBitmap = obj

                Log.d("IMAGE", "NO EMPTY")

                val x = 50 // Posición inicial X
                val y = 50 // Posición Y fija para todos los bitmaps

                val originalWidth = data.image.width.toFloat()
                val originalHeight = data.image.height.toFloat()

                val maxWidth = 1000f // Ancho máximo permitido
                val maxHeight = 1000f // Altura máxima permitida

                // Calcula las proporciones para ajustar el ancho y el alto del bitmap
                val ratio = originalWidth / originalHeight
                var newWidth = if (originalWidth > maxWidth) maxWidth else originalWidth
                var newHeight = newWidth / ratio

                // Verifica si la altura supera el límite permitido
                if (newHeight > maxHeight) {
                    val scaleRatio = maxHeight / newHeight
                    newHeight *= scaleRatio
                    newWidth *= scaleRatio
                }

                // Crea el rectángulo de destino con la posición X actual, posición Y y tamaño del bitmap ajustado
                val dstRect = RectF(
                    x.toFloat(),
                    y.toFloat(),
                    x + newWidth,
                    y + newHeight
                )


                val myImage = ImageBitmap2(
                    data.image, x.toFloat(), y.toFloat(),
                    newWidth, newHeight
                )

                // Dibuja el bitmap en el canvas en la posición y tamaño especificados
                mBufferCanvas?.drawBitmap(data.image, null, dstRect, null)

                myLines.add(MyLine(null, null, null, myImage))

                render()
            }

            WriteCommand.DRAW_LINE_ACCELERATE -> {
                val obj = msg.obj as? LineData ?: return true
                val data: LineData = obj
                myLines.add(MyLine(data.getPoints(), null, data.paint, null))
                mBufferCanvas?.drawPath(data.toPath(), data.paint.toPaint())
            }

            WriteCommand.ERASER_ACCELERATE -> {
                val obj = msg.obj as? EraseData ?: return true
                val data: EraseData = obj
                myLines.add(MyLine(null, data.regions, mEraserPaint, null))
                for (region in data.regions) {
                    if (region != null) {
                        mBufferCanvas?.drawRect(region, mEraserPaint.toPaint())
                    }
                }
            }

            WriteCommand.ACCELERATE_FINISH_REQUEST_RENDER -> {
                render()
                AccelerateManager.instance.delaySyncLayer()
            }
        }
        return true
    }

    fun onRender(canvas: Canvas) {
        Log.d(TAG, "onRender()")
        canvas.drawBitmap(GlobalConfig.backgroundBitmap, 0f, 0f, null)
        mDisplayBitmap?.let { canvas.drawBitmap(it, 0f, 0f, null) }
        // También puedes agregar aquí otras operaciones de dibujo específicas en el lienzo.
        // Por ejemplo, dibujar elementos adicionales sobre el lienzo principal.
    }

    private fun render() {
        Log.d(TAG, "RENDER")
        // Realizar otras operaciones de dibujo o lógica si es necesario.
        // Por ejemplo, dibujar trazos adicionales en mBufferCanvas.

        // Dibujar el búfer en el lienzo de visualización.
        mDisplayCanvas?.drawBitmap(mBufferBitmap!!, 0f, 0f, null)

        // Llamar al callback para notificar que el dibujo ha sido actualizado.
        callBack()
    }

    fun resize(width: Int, height: Int) {
        mBufferBitmap?.recycle() // Liberar el antiguo búfer de dibujo
        mBufferBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        mBufferCanvas = Canvas(mBufferBitmap!!)

        mDisplayBitmap?.recycle() // Liberar el antiguo búfer de visualización
        mDisplayBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        mDisplayCanvas = Canvas(mDisplayBitmap!!)
    }
    fun createDrawLineActor(id: Int): DrawLineActor {
        return AccelerateDrawLineActor(this, id, GlobalConfig.sPenColor, GlobalConfig.sPenWidth)
    }

    fun createEraserActor(id: Int): EraserActor {
        return AccelerateEraserActor(this, id, GlobalConfig.sEraserWidth, GlobalConfig.sEraserHeight)
    }

    fun accelerateFinishRequestRender() {
        mHandler.obtainMessage(WriteCommand.ACCELERATE_FINISH_REQUEST_RENDER).sendToTarget()
    }

    fun clearWhiteboard() {
        mHandler.obtainMessage(WriteCommand.CLEAN).sendToTarget()
    }

    fun saveWhiteboard(lines: (MyLines) -> Unit) {
        lines(
            MyLines(
                myLines,
                GlobalConfig.backgroundWallpaper,
                GlobalConfig.backgroundColor,
                123,
                GlobalConfig.currentPage
            )
        )
        mHandler.obtainMessage(WriteCommand.CLEAN).sendToTarget()
    }

    private fun clear() {
        mBufferBitmap?.recycle() // Liberar el búfer de dibujo
        mDisplayBitmap?.recycle() // Liberar el búfer de visualización

        // Crear un nuevo búfer de dibujo y visualización
        mBufferBitmap = Bitmap.createBitmap(GlobalConfig.SCREEN_WIDTH, GlobalConfig.SCREEN_HEIGHT, Bitmap.Config.ARGB_8888)
        mBufferCanvas = Canvas(mBufferBitmap!!)
        mDisplayBitmap = Bitmap.createBitmap(GlobalConfig.SCREEN_WIDTH, GlobalConfig.SCREEN_HEIGHT, Bitmap.Config.ARGB_8888)
        mDisplayCanvas = Canvas(mDisplayBitmap!!)

        // Establecer el fondo predeterminado en el nuevo búfer de dibujo
        GlobalConfig.backgroundBitmap = Bitmap.createBitmap(
            GlobalConfig.SCREEN_WIDTH, GlobalConfig.SCREEN_HEIGHT, Bitmap.Config.ARGB_8888
        ).apply {
            val canvas = Canvas(this)
            canvas.drawColor(GlobalConfig.defaultBackgroundColor)
        }

        // Restaurar otras variables y listas
        GlobalConfig.backgroundWallpaper = null
        GlobalConfig.backgroundColor = null
        myLines = mutableListOf()
        myUndoLines = mutableListOf()
        myLinesHistory = mutableListOf()

        render()
    }

    private fun clearToRender() {
        // Crear un nuevo búfer de dibujo y visualización con el mismo tamaño
        mBufferBitmap = Bitmap.createBitmap(GlobalConfig.SCREEN_WIDTH, GlobalConfig.SCREEN_HEIGHT, Bitmap.Config.ARGB_8888)
        mBufferCanvas = Canvas(mBufferBitmap!!)
        mDisplayBitmap = Bitmap.createBitmap(GlobalConfig.SCREEN_WIDTH, GlobalConfig.SCREEN_HEIGHT, Bitmap.Config.ARGB_8888)
        mDisplayCanvas = Canvas(mDisplayBitmap!!)

        // Llamar a render() para actualizar la interfaz de usuario con el nuevo búfer de visualización
        render()
    }

    fun getLinesWhiteboard(lines: (MyLines) -> Unit) {
        lines(
            MyLines(
                myLines,
                GlobalConfig.backgroundWallpaper,
                GlobalConfig.backgroundColor,
                123,
                GlobalConfig.currentPage
            )
        )
    }

    companion object {
        private const val TAG = "chenw:;WriteBoardController"
    }
}