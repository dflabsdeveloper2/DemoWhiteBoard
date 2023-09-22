package com.skg.drawaccelerate

import android.os.Handler
import android.os.HandlerThread
import android.os.Message

// 等待android已经完成渲染，再清除加速图层, 避免中间出现间隔闪烁
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