package com.skg.drawaccelerate

import android.os.Handler
import android.os.HandlerThread
import android.os.Message

// Esperar a que Android termine de renderizar antes de borrar la capa de aceleración para evitar parpadeos intermitentes.
class DelaySyncLayer(private val mDelayTime: Long) {
    private val mHandlerThread = HandlerThread("delay")
    private val mHandler: Handler

    init {
        mHandlerThread.start()
        mHandler = object : Handler(mHandlerThread.looper) {
            override fun handleMessage(msg: Message) {
                if (msg.what == 0) {
                    AccelerateManager.instance.syncLayer()
                }
            }
        }
    }

    fun reset() {
        mHandler.removeMessages(0)
    }

    operator fun invoke() {
        mHandler.removeMessages(0)
        mHandler.sendEmptyMessageDelayed(0, mDelayTime)
    }
}