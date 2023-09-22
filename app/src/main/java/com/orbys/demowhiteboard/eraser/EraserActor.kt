package com.orbys.demowhiteboard.eraser

import android.graphics.RectF
import com.orbys.demowhiteboard.whiteboard.WriteBoardController


abstract class EraserActor(
    mController: WriteBoardController,
    id: Int,
    width: Float,
    height: Float
) {
    protected var mWriteBoardController: WriteBoardController
    private val mId: Int
    protected val mEraserWidth: Float
    protected val mEraserHeight: Float

    init {
        mWriteBoardController = mController
        mId = id
        mEraserWidth = width
        mEraserHeight = height
    }

    abstract fun onDownEvent(x: Float, y: Float)
    abstract fun onMoveEvent(x: Float, y: Float)
    abstract fun onUpEvent(x: Float, y: Float)
    fun getEraserRect(x: Float, y: Float): RectF {
        val halfW = mEraserWidth / 2
        val halfH = mEraserHeight / 2
        return RectF(x - halfW, y - halfH, x + halfW, y + halfH)
    }
}