package com.orbys.demowhiteboard

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

object GlobalConfig {

    /**
     * 0 -> draw
     * 1 -> eraser
     */
    var sMode = 0
    var sPenWidth = 10f
    var sPenColor = Color.RED
    var sAccelerate = true
    var sEraserWidth = 120f
    var sEraserHeight = 160f
    private var SCREEN_HEIGHT = 2160
    private var SCREEN_WIDTH = 3840
    private var BOX_SIZE = 200
    val background: Bitmap = Bitmap.createBitmap(
        SCREEN_WIDTH, SCREEN_HEIGHT,
        Bitmap.Config.ARGB_8888
    ).apply {
        val canvas = Canvas(this)
        canvas.drawColor(-0xffa6b0)
        val paint = Paint()
        paint.color = -0xffcdd3

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