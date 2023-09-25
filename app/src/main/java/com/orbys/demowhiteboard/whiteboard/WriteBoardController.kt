package com.orbys.demowhiteboard.whiteboard

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import com.google.gson.Gson
import com.orbys.demowhiteboard.GlobalConfig
import com.skg.drawaccelerate.AccelerateManager
import com.orbys.demowhiteboard.drawline.AccelerateDrawLineActor
import com.orbys.demowhiteboard.drawline.DrawLineActor
import com.orbys.demowhiteboard.drawline.LineData
import com.orbys.demowhiteboard.eraser.AccelerateEraserActor
import com.orbys.demowhiteboard.eraser.EraseData
import com.orbys.demowhiteboard.eraser.EraserActor
import com.orbys.demowhiteboard.model.MyLine
import com.orbys.demowhiteboard.model.MyLineEraser
import com.orbys.demowhiteboard.model.MyLines
import java.io.File
import java.io.FileWriter

class WriteBoardController(private val callBack: () -> Unit) : Handler.Callback {
    private var mBaseBitmap: Bitmap? = null
    private var mBaseCanvas: Canvas? = null
    private var mStrokesBitmap: Bitmap? = null
    private var mStrokesCanvas: Canvas? = null
    private val mHandlerThread: HandlerThread = HandlerThread("temp-r")
    private val mHandler: Handler
    //private val mEraserIndicatorPaint = Paint()
    private val mEraserPaint = Paint()
    private var myLines:MutableList<MyLine>
    private var myLineEraser:MutableList<MyLineEraser>

    init {
        mHandlerThread.start()
        mHandler = Handler(mHandlerThread.looper, this)
        //mEraserIndicatorPaint.color = Color.WHITE
        mEraserPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        myLines = mutableListOf()
        myLineEraser = mutableListOf()
    }

    fun drawLineAccelerate(data: LineData?) {
        mHandler.obtainMessage(WriteCommand.DRAW_LINE_ACCELERATE, data).sendToTarget()
    }

    fun eraseAccelerate(data: EraseData?) {
        mHandler.obtainMessage(WriteCommand.ERASER_ACCELERATE, data).sendToTarget()
    }

    fun debug(){
        mHandler.obtainMessage(WriteCommand.DEBUG_LINE).sendToTarget()
    }

    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            WriteCommand.DEBUG_LINE ->{
                myLines.forEach {
                    Log.d("LINES","linia: $it")
                }
                myLineEraser.forEach {
                    Log.d("LINES","linia BORRADO: $it")
                }

            }

            WriteCommand.CLEAN -> {
                //mStrokesCanvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                mStrokesBitmap = null
                mStrokesCanvas = null
                myLines= mutableListOf()
                myLineEraser = mutableListOf()
                resize(GlobalConfig.SCREEN_WIDTH,GlobalConfig.SCREEN_HEIGHT)
                render()
            }
            WriteCommand.DRAW_LINE_ACCELERATE -> {
                val obj = msg.obj as? LineData ?: return true
                val data: LineData = obj
                Log.d(TAG,"data: ${data.getPoints()}")
                myLines.add(MyLine(data.getPoints(),data.paint))
                mStrokesCanvas?.drawPath(data.toPath(), data.paint)
            }
            WriteCommand.ERASER_ACCELERATE -> {
                val obj = msg.obj as? EraseData ?: return true
                val data: EraseData = obj
                myLineEraser.add(MyLineEraser(data.regions,mEraserPaint))
                for (region in data.regions) {
                    if (region != null) {
                        Log.d(TAG,"region: $region")
                        mStrokesCanvas?.drawRect(region, mEraserPaint)
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
        Log.d(TAG,"onRender()")
        canvas.drawBitmap(GlobalConfig.background, 0f, 0f, null)
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

    fun clearWhiteboard(){
        mHandler.obtainMessage(WriteCommand.CLEAN).sendToTarget()
    }

    fun saveWhiteboard(saved:(String)->Unit){
        val gson = Gson()
        val json = gson.toJson(MyLines(myLines,myLineEraser,1233,123,1))
        saved(json)
    }

    companion object {
        private const val TAG = "chenw:;WriteBoardController"
    }
}