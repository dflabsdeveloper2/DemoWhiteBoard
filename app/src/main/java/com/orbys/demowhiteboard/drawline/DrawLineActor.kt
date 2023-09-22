package com.orbys.demowhiteboard.drawline

import com.orbys.demowhiteboard.whiteboard.WriteBoardController

abstract class DrawLineActor(mController: WriteBoardController, id: Int, color: Int, width: Float) {
    protected var mWriteBoardController: WriteBoardController
    protected val mId: Int
    protected var mColor: Int
    protected var mWidth: Float

    init {
        mWriteBoardController = mController
        mId = id
        mColor = color
        mWidth = width
    }

    abstract fun onFirstFingerDown()
    abstract fun onDownEvent(x: Float, y: Float)
    abstract fun onMoveEvent(x: Float, y: Float)
    abstract fun onUpEvent(x: Float, y: Float)
    abstract fun onLastFingerUp()
}