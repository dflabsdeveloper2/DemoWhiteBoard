package com.orbys.demowhiteboard.domain.eraser

import android.util.Log
import android.view.MotionEvent
import com.orbys.demowhiteboard.ui.interfaz.Distribute
import com.orbys.demowhiteboard.ui.whiteboard.WriteBoardController

class EraserDistribute(mController: WriteBoardController) :
    Distribute {
    private var mActiveActor: EraserActor? = null
    private val mWriteBoardController: WriteBoardController

    init {
        mWriteBoardController = mController
    }

    override fun onTouchEvent(event: MotionEvent) {
        val action: Int = event.actionMasked
        val actionId: Int = event.getPointerId(0)
        val x: Float = event.getX(0)
        val y: Float = event.getY(0)
        if (action == MotionEvent.ACTION_DOWN) {
            mActiveActor = mWriteBoardController.createEraserActor(actionId)
        }
        if (action == MotionEvent.ACTION_MOVE) {
            if (mActiveActor != null) {
                mActiveActor!!.onMoveEvent(x, y)
            }
        } else {
            if (mActiveActor == null) {
                Log.d("error", "#1024")
                return
            }
            if (action == MotionEvent.ACTION_DOWN) {
                mActiveActor!!.onDownEvent(x, y)
            } else if (action == MotionEvent.ACTION_UP) {
                mActiveActor!!.onUpEvent(x, y)
            }
        }
    }
}