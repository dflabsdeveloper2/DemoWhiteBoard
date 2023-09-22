package com.orbys.demowhiteboard.eraser

import com.orbys.demowhiteboard.whiteboard.WriteBoardController

class SoftEraserActor(mController: WriteBoardController, id: Int, width: Float, height: Float) :
    EraserActor(mController, id, width, height) {
    override fun onDownEvent(x: Float, y: Float) {
        val data = SoftEraseData(true, getEraserRect(x, y))
        mWriteBoardController.eraseSoft(data)
    }

    override fun onMoveEvent(x: Float, y: Float) {
        val data = SoftEraseData(true, getEraserRect(x, y))
        mWriteBoardController.eraseSoft(data)
    }

    override fun onUpEvent(x: Float, y: Float) {
        val data = SoftEraseData(false, getEraserRect(x, y))
        mWriteBoardController.eraseSoft(data)
    }
}