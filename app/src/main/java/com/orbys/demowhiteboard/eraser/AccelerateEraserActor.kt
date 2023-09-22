package com.orbys.demowhiteboard.eraser

import com.orbys.demowhiteboard.GlobalConfig
import com.skg.drawaccelerate.AccelerateManager
import com.orbys.demowhiteboard.whiteboard.WriteBoardController


class AccelerateEraserActor(
    mController: WriteBoardController,
    id: Int,
    width: Float,
    height: Float
) : EraserActor(mController, id, width, height) {
    private val mEraseData = EraseData()
    override fun onDownEvent(x: Float, y: Float) {
        AccelerateManager.instance
            .eraserStart(x, y, mEraserWidth, mEraserHeight, GlobalConfig.background)
        mEraseData.addRegion(getEraserRect(x, y))
    }

    override fun onMoveEvent(x: Float, y: Float) {
        AccelerateManager.instance.eraserMove(x, y, GlobalConfig.background)
        mEraseData.addRegion(getEraserRect(x, y))
    }

    override fun onUpEvent(x: Float, y: Float) {
        AccelerateManager.instance.eraserStop(GlobalConfig.background)
        mEraseData.addRegion(getEraserRect(x, y))
        mWriteBoardController.eraseAccelerate(mEraseData)
        mWriteBoardController.accelerateFinishRequestRender()
    }
}