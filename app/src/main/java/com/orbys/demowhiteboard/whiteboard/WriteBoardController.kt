package com.orbys.demowhiteboard.whiteboard

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import com.orbys.demowhiteboard.GlobalConfig
import com.skg.drawaccelerate.AccelerateManager
import com.orbys.demowhiteboard.drawline.AccelerateDrawLineActor
import com.orbys.demowhiteboard.drawline.DrawLineActor
import com.orbys.demowhiteboard.drawline.LineData
import com.orbys.demowhiteboard.eraser.AccelerateEraserActor
import com.orbys.demowhiteboard.eraser.EraseData
import com.orbys.demowhiteboard.eraser.EraserActor

class WriteBoardController(private val callBack: () -> Unit) : Handler.Callback {
    private var mBaseBitmap: Bitmap? = null
    private var mBaseCanvas: Canvas? = null
    private var mStrokesBitmap: Bitmap? = null
    private var mStrokesCanvas: Canvas? = null
    private val mHandlerThread: HandlerThread = HandlerThread("temp-r")
    private val mHandler: Handler
    //private val mEraserIndicatorPaint = Paint()
    private val mEraserPaint = Paint()

    init {
        mHandlerThread.start()
        mHandler = Handler(mHandlerThread.looper, this)
        //mEraserIndicatorPaint.color = Color.WHITE
        mEraserPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    fun drawLineAccelerate(data: LineData?) {
        mHandler.obtainMessage(WriteCommand.DRAW_LINE_ACCELERATE, data).sendToTarget()
    }

    fun eraseAccelerate(data: EraseData?) {
        mHandler.obtainMessage(WriteCommand.ERASER_ACCELERATE, data).sendToTarget()
    }

    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            WriteCommand.CLEAN -> {
                mStrokesCanvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            }
            WriteCommand.DRAW_LINE_ACCELERATE -> {
                val obj = msg.obj as? LineData ?: return true
                val data: LineData = obj
                mStrokesCanvas?.drawPath(data.toPath(), data.paint)
            }
            WriteCommand.ERASER_ACCELERATE -> {
                val obj = msg.obj as? EraseData ?: return true
                val data: EraseData = obj
                for (region in data.regions) {
                    if (region != null) {
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

    companion object {
        private const val TAG = "chenw:;WriteBoardController"
    }
}