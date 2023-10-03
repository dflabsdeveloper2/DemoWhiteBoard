package com.orbys.demowhiteboard.whiteboard

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import android.widget.Toast
import com.orbys.demowhiteboard.GlobalConfig
import com.orbys.demowhiteboard.drawline.AccelerateDrawLineActor
import com.orbys.demowhiteboard.drawline.DrawLineActor
import com.orbys.demowhiteboard.drawline.LineData
import com.orbys.demowhiteboard.eraser.AccelerateEraserActor
import com.orbys.demowhiteboard.eraser.EraseData
import com.orbys.demowhiteboard.eraser.EraserActor
import com.orbys.demowhiteboard.model.MyLine
import com.orbys.demowhiteboard.model.MyLines
import com.orbys.demowhiteboard.model.MyPaint
import com.orbys.demowhiteboard.ui.core.Util
import com.skg.drawaccelerate.AccelerateManager

class WriteBoardController(private val context:Context, private val callBack: () -> Unit) : Handler.Callback {
    private var mBaseBitmap: Bitmap? = null
    private var mBaseCanvas: Canvas? = null
    private var mStrokesBitmap: Bitmap? = null
    private var mStrokesCanvas: Canvas? = null
    private val mHandlerThread: HandlerThread = HandlerThread("temp-r")
    private val mHandler: Handler

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

    fun undoAction(){
        mHandler.obtainMessage(WriteCommand.UNDO).sendToTarget()
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
                    val lastUndoneLine = myUndoLines.removeAt(myUndoLines.size - 1)
                    myLines.add(lastUndoneLine)

                    myLines.forEach {
                        if (!it.props.ereaser) {
                            val lineData = LineData(it.props.color, it.props.strokeWidth)
                            it.line!!.forEach { point ->
                                lineData.addPoint(point.x, point.y)
                            }
                            mStrokesCanvas?.drawPath(lineData.toPath(), it.props.toPaint())
                        } else {
                            it.lineEraser!!.forEach {rect->
                                if (rect != null) {
                                    mStrokesCanvas?.drawRect(rect, it.props.toPaint())
                                }
                            }
                        }
                    }
                    render()
                }else{
                    Toast.makeText(context,"No hay mas registros",Toast.LENGTH_SHORT).show()
                }
            }
            WriteCommand.UNDO ->{
                Log.d("UNDO", "UNDO")
                myLinesHistory = myLines
                if(myLinesHistory.isNotEmpty()){
                    myUndoLines.add(myLinesHistory[myLinesHistory.size-1])
                    Log.d("UNDO", "myLinesHistory ${myLinesHistory.size}")
                   clearToRender()

                    val listRedo = myLinesHistory.dropLast(1)

                    listRedo.forEach {
                        if (!it.props.ereaser) {
                            val lineData = LineData(it.props.color, it.props.strokeWidth)
                            it.line!!.forEach { point ->
                                lineData.addPoint(point.x, point.y)
                            }
                            mStrokesCanvas?.drawPath(lineData.toPath(), it.props.toPaint())
                        } else {
                            it.lineEraser!!.forEach {rect->
                                if (rect != null) {
                                    mStrokesCanvas?.drawRect(rect, it.props.toPaint())
                                }
                            }
                        }
                    }

                    render()

                    myLines = listRedo.toMutableList()
                }else{
                    Toast.makeText(context,"No hay mas registros",Toast.LENGTH_SHORT).show()
                }
            }

            WriteCommand.DRAW_SAVED -> {
                clear()

                Log.d("SAVE", "OPEN saved whiteboard")
                val obj = msg.obj as? MyLines ?: return true

                GlobalConfig.backgroundWallpaper = obj.backgroundWallpaper
                GlobalConfig.backgroundColor = obj.backgroundColor
                GlobalConfig.page = obj.page

                val lineDraw = obj.listLines

                val bitmapWallpaper =
                    GlobalConfig.backgroundWallpaper?.let { Util.createBitmapFromBase64String(it) }


                //establecer bitmap wallpaper
                GlobalConfig.backgroundBitmap = Bitmap.createBitmap(
                    GlobalConfig.SCREEN_WIDTH, GlobalConfig.SCREEN_HEIGHT,
                    Bitmap.Config.ARGB_8888
                ).apply {
                    if (bitmapWallpaper != null) {
                        val canvas = Canvas(this)
                        canvas.scale(
                            width.toFloat() / bitmapWallpaper.width,
                            height.toFloat() / bitmapWallpaper.height
                        )
                        canvas.drawBitmap(bitmapWallpaper, 0f, 0f, null)
                    } else {
                        val canvas = Canvas(this)
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
                    if (!it.props.ereaser) {
                        val lineData = LineData(it.props.color, it.props.strokeWidth)
                        it.line!!.forEach { point ->
                            lineData.addPoint(point.x, point.y)
                        }
                        offscreenCanvas.drawPath(lineData.toPath(), it.props.toPaint())
                    } else {
                        it.lineEraser!!.forEach {rect->
                            if (rect != null) {
                                offscreenCanvas.drawRect(rect, it.props.toPaint())
                            }
                        }
                    }
                }

                mStrokesCanvas?.drawBitmap(offscreenBitmap, 0f, 0f, null)

                render()

                myLines = lineDraw.toMutableList()
            }

            WriteCommand.CLEAN -> {
                //mStrokesCanvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                clear()
            }

            WriteCommand.DRAW_LINE_ACCELERATE -> {
                val obj = msg.obj as? LineData ?: return true
                val data: LineData = obj
                myLines.add(MyLine(data.getPoints(), null, data.paint))
                mStrokesCanvas?.drawPath(data.toPath(), data.paint.toPaint())
            }

            WriteCommand.ERASER_ACCELERATE -> {
                val obj = msg.obj as? EraseData ?: return true
                val data: EraseData = obj
                myLines.add(MyLine(null, data.regions, mEraserPaint))
                for (region in data.regions) {
                    if (region != null) {
                        mStrokesCanvas?.drawRect(region, mEraserPaint.toPaint())
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
        mStrokesBitmap?.let { canvas.drawBitmap(it, 0f, 0f, null) }
    }

    private fun render() {
        callBack()
    }

    fun resize(width: Int, height: Int) {
        mStrokesBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        mStrokesCanvas = Canvas(mStrokesBitmap!!)
        mBaseBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        mBaseCanvas = Canvas(mBaseBitmap!!)
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
                GlobalConfig.page
            )
        )
        mHandler.obtainMessage(WriteCommand.CLEAN).sendToTarget()
    }

    private fun clear() {
        GlobalConfig.backgroundBitmap = Bitmap.createBitmap(
            GlobalConfig.SCREEN_WIDTH, GlobalConfig.SCREEN_HEIGHT,
            Bitmap.Config.ARGB_8888
        ).apply {
            val canvas = Canvas(this)
            canvas.drawColor(GlobalConfig.defaultBackgroundColor)
        }
        mStrokesBitmap = null
        mStrokesCanvas = null
        GlobalConfig.backgroundWallpaper = null
        GlobalConfig.backgroundColor = null
        myLines = mutableListOf()
        myUndoLines = mutableListOf()
        myLinesHistory = mutableListOf()
        resize(GlobalConfig.SCREEN_WIDTH, GlobalConfig.SCREEN_HEIGHT)
        render()
    }


    private fun clearToRender(){
        resize(GlobalConfig.SCREEN_WIDTH, GlobalConfig.SCREEN_HEIGHT)
        render()
    }
    companion object {
        private const val TAG = "chenw:;WriteBoardController"
    }
}