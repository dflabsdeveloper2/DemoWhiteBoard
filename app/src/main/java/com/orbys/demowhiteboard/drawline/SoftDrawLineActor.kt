package com.orbys.demowhiteboard.drawline

import com.orbys.demowhiteboard.whiteboard.WriteBoardController

class SoftDrawLineActor(mController: WriteBoardController, id: Int, color: Int, width: Float) :
    DrawLineActor(mController, id, color, width) {
    private val mLineData = SoftLine(LineData(mColor, mWidth))
    override fun onFirstFingerDown() {}
    override fun onDownEvent(x: Float, y: Float) {
        val data = mLineData.addPoint(x, y)
        mWriteBoardController.drawLineSoft(data)
    }

    override fun onMoveEvent(x: Float, y: Float) {
        val data = mLineData.addPoint(x, y)
        mWriteBoardController.drawLineSoft(data)
    }

    override fun onUpEvent(x: Float, y: Float) {
        val data = mLineData.addPoint(x, y)
        mWriteBoardController.drawLineSoft(data)
    }

    override fun onLastFingerUp() {}
}