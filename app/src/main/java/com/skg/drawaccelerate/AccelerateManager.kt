package com.skg.drawaccelerate

import android.graphics.Bitmap
import android.util.Log

enum class AccelerateManager {
    INS;

    private val mDelaySyncer = DelaySyncLayer(300)
    private var mInitResult = -1

    fun createPaint(id: Int, color: Int, width: Float) {
        enforceInitSuccess()
        NativeInterface.createPaint(id, color, width)
    }

    fun deletePaint(id: Int) {
        enforceInitSuccess()
        NativeInterface.deletePaint(id)
    }

    fun drawStart() {
        enforceInitSuccess()
        mDelaySyncer.reset()
        NativeInterface.drawStart()
    }

    fun drawDown(id: Int, x: Float, y: Float) {
        enforceInitSuccess()
        NativeInterface.drawDown(id, x, y)
    }

    fun drawPoints(id: Int, points: FloatArray?) {
        enforceInitSuccess()
        NativeInterface.drawPoints(id, points)
    }

    fun drawUp(id: Int, x: Float, y: Float) {
        enforceInitSuccess()
        NativeInterface.drawUp(id, x, y)
    }

    fun drawFinish() {
        enforceInitSuccess()
        NativeInterface.drawFinish()
    }

    fun eraserStart(x: Float, y: Float, size_w: Float, size_h: Float, bg: Bitmap?) {
        enforceInitSuccess()
        mDelaySyncer.reset()
        NativeInterface.eraserStart(x, y, size_w, size_h, bg)
    }

    fun eraserMove(x: Float, y: Float, bg: Bitmap?) {
        enforceInitSuccess()
        NativeInterface.eraserMove(x, y, bg)
    }

    fun eraserStop(bg: Bitmap?) {
        enforceInitSuccess()
        NativeInterface.eraserStop(bg)
    }

    fun delaySyncLayer() {
        mDelaySyncer.invoke()
    }

    fun syncLayer() {
        NativeInterface.syncLayer()
    }

    private fun enforceInitSuccess() {
        check(mInitResult == 0) { "init failed state=$mInitResult" }
    }

    // todo lifecycle observer
    fun onCreate() {
        try {
            NativeInterface.notifyLifecycleEvent(0)
        }catch (e:Exception){
            Log.d("ACCELERATE","Error al cargar la libreria")
        }
    }

    fun onStart() {
        NativeInterface.notifyLifecycleEvent(1)
        mInitResult = NativeInterface.accelerateInit()
    }

    fun onResume() {
        NativeInterface.notifyLifecycleEvent(2)
    }

    fun onPause() {
        NativeInterface.notifyLifecycleEvent(3)
    }

    fun onStop() {
        NativeInterface.notifyLifecycleEvent(4)
        mInitResult = -1
        NativeInterface.accelerateDeinit()
    }

    fun onDestroy() {
        NativeInterface.notifyLifecycleEvent(5)
    }

    companion object {
        val instance: AccelerateManager
            get() = INS
        private const val TAG = "chenw::AccelerateManager"
    }
}