package com.orbys.demowhiteboard.whiteboard

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.orbys.demowhiteboard.Distribute
import com.orbys.demowhiteboard.GlobalConfig
import com.orbys.demowhiteboard.drawline.DrawLineDistribute
import com.orbys.demowhiteboard.eraser.EraserDistribute
import com.orbys.demowhiteboard.model.MyLines

class WriteBoard(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private val mController = WriteBoardController { this.postInvalidate() }
    private val mDrawLineDistribute: DrawLineDistribute = DrawLineDistribute(mController)
    private val mEraserDistribute: EraserDistribute = EraserDistribute(mController)
    private var mActiveDistribute: Distribute = mDrawLineDistribute

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.d(TAG,"RESIZE")
        mController.resize(w, h)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val actionMasked: Int = event.actionMasked
        if (actionMasked == MotionEvent.ACTION_DOWN) {
            if (GlobalConfig.sMode == 0) {
                mActiveDistribute = mDrawLineDistribute
            } else if (GlobalConfig.sMode == 1) {
                mActiveDistribute = mEraserDistribute
            }
        }
        mActiveDistribute.onTouchEvent(event)
        return true
    }

    override fun onDraw(canvas: Canvas) {
        mController.onRender(canvas)
    }

    companion object {
        private const val TAG = "chenw::WriteBoard"
    }

    fun clean() {
        mController.clearWhiteboard()
    }

    fun debugCall() {
        mController.debug()
    }

    fun saveCall(lines: (MyLines) -> Unit) {
        mController.saveWhiteboard { lines(it) }
    }

    fun drawSavedJson(data:MyLines){
        mController.drawSaved(data)
    }

    fun redoBtn(){
        mController.redoAction()
    }
}