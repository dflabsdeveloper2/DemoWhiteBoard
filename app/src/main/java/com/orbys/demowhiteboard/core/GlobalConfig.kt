package com.orbys.demowhiteboard.core

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color

object GlobalConfig {

    /**
     * 0 -> draw
     * 1 -> eraser
     */
    //VAlores Finales
    var sMode = 0
    var sPenWidth = 10f
    var sPenColor = Color.RED
    const val sEraserWidth = 160f
    const val sEraserHeight = 200f
    const val SCREEN_HEIGHT = 2160
    const val SCREEN_WIDTH = 3840
    const val LIMIT_PAGES = 10
    const val defaultBackgroundColor: Int = -0xffa6b0


    var backgroundColor:Int? = defaultBackgroundColor
    var backgroundWallpaper: String? = null
    var page = 1
    var listImages:MutableList<Bitmap>? = null


    var backgroundBitmap: Bitmap = Bitmap.createBitmap(
        SCREEN_WIDTH, SCREEN_HEIGHT,
        Bitmap.Config.ARGB_8888
    ).apply {
        backgroundColor?.let {
            val canvas = Canvas(this)
            canvas.drawColor(it)
        }
    }

    //VALORES PEN
    var penWidthFino = 10f
    var penWidthGrueso = 60f
    var penColorFino = Color.RED
    var penColorGrueso = Color.BLACK
}