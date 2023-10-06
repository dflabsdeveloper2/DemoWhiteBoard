package com.orbys.demowhiteboard.domain.drawline

import com.orbys.demowhiteboard.ui.whiteboard.WriteBoardController
import com.skg.drawaccelerate.AccelerateManager


class AccelerateDrawLineActor(
    mController: WriteBoardController,
    id: Int,
    color: Int,
    width: Float
) : DrawLineActor(mController, id, color, width) {
    private val mLineData = LineData(mColor, mWidth)
    override fun onFirstFingerDown() {
        AccelerateManager.instance.drawStart()
    }

    override fun onDownEvent(x: Float, y: Float) {
        AccelerateManager.instance.createPaint(mId, mColor, mWidth)
        AccelerateManager.instance.drawDown(mId, x, y)
        mLineData.addPoint(x, y)
    }

    override fun onMoveEvent(x: Float, y: Float) {
        val floats = mLineData.addPoint(x, y)
        AccelerateManager.instance.drawPoints(mId, floats)
    }

    override fun onUpEvent(x: Float, y: Float) {
        mLineData.addLastPoint(x, y)
        AccelerateManager.instance.drawUp(mId, x, y)
        mWriteBoardController.drawLineAccelerate(mLineData)
        AccelerateManager.instance.deletePaint(mId)
    }

    override fun onLastFingerUp() {
        AccelerateManager.instance.drawFinish()
        mWriteBoardController.accelerateFinishRequestRender()
    }
}