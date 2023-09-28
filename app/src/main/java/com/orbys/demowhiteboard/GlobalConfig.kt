package com.orbys.demowhiteboard

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color

object GlobalConfig {

    /**
     * 0 -> draw
     * 1 -> eraser
     */
    var sMode = 0
    var sPenWidth = 10f
    var sPenColor = Color.RED
    var sEraserWidth = 160f
    var sEraserHeight = 200f
    var SCREEN_HEIGHT = 2160
    var SCREEN_WIDTH = 3840
    var backgroundColor = -0xffa6b0
    private var BOX_SIZE = 200
    var backgroundBitmap: Bitmap = Bitmap.createBitmap(
        SCREEN_WIDTH, SCREEN_HEIGHT,
        Bitmap.Config.ARGB_8888
    ).apply {
        val canvas = Canvas(this)
        canvas.drawColor(backgroundColor)
       /* val paint = Paint()
        paint.color = -0xffcdd3*/

       /* RAYAS EN LA PANTALLA

        paint.style = Paint.Style.STROKE

        var i = 0
        while (i < SCREEN_HEIGHT) {
            canvas.drawLine(0f, i.toFloat(), SCREEN_WIDTH.toFloat(), i.toFloat(), paint)
            i += BOX_SIZE
        }

        i = 0
        while (i < SCREEN_WIDTH) {
            canvas.drawLine(i.toFloat(), 0f, i.toFloat(), SCREEN_HEIGHT.toFloat(), paint)
            i += BOX_SIZE
        }*/
    }
}