package com.orbys.demowhiteboard.domain

import android.graphics.Bitmap
import android.graphics.Canvas
import com.orbys.demowhiteboard.whiteboard.WriteBoard

object BitmapWhiteboard {

    fun getBitmapWhiteBoard(whiteboard: WriteBoard):Bitmap{
        val whiteboardBitmap = Bitmap.createBitmap(
            whiteboard.width,
            whiteboard.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(whiteboardBitmap)
        whiteboard.draw(canvas)
        return whiteboardBitmap
    }
}