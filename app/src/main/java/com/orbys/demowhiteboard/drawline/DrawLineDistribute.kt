package com.orbys.demowhiteboard.drawline

import android.util.Log
import android.util.SparseArray
import android.view.MotionEvent
import com.orbys.demowhiteboard.Distribute
import com.orbys.demowhiteboard.ui.core.Util
import com.orbys.demowhiteboard.whiteboard.WriteBoardController

class DrawLineDistribute(mController: WriteBoardController) : Distribute {
    private var mActiveLines = SparseArray<DrawLineActor>()
    private val mWriteBoardController: WriteBoardController

    init {
        mWriteBoardController = mController
    }

    override fun onTouchEvent(event: MotionEvent) {
        var actionIndex: Int = event.actionIndex
        val size = event.size
        val action: Int = event.actionMasked
        if (size< Util.thickPointSize){

        }
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_UP) {
            actionIndex = 0
        }
        val actionId: Int = event.getPointerId(actionIndex)
        val x: Float = event.getX(actionIndex)
        val y: Float = event.getY(actionIndex)
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            mActiveLines.put(actionId, mWriteBoardController.createDrawLineActor(actionId))
        }
        if (action == MotionEvent.ACTION_MOVE) {
            for (i in 0 until event.pointerCount) {
                val cid: Int = event.getPointerId(i)
                val historySize: Int = event.historySize
                val currentAction = mActiveLines[cid]
                for (i1 in 0 until historySize) {
                    currentAction?.onMoveEvent(
                        event.getHistoricalX(i, i1),
                        event.getHistoricalY(i, i1)
                    )
                }
            }
        } else {
            val actor = mActiveLines[actionId]
            if (actor == null) {
                Log.d("error", "#1024")
                return
            }
            if (action == MotionEvent.ACTION_DOWN) {
                actor.onFirstFingerDown()
            }
            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
                actor.onDownEvent(x, y)
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
                actor.onUpEvent(x, y)
            }
            if (action == MotionEvent.ACTION_UP) {
                actor.onLastFingerUp()
            }
        }
    }
}