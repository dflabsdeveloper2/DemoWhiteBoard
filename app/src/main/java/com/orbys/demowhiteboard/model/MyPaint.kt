package com.orbys.demowhiteboard.model

import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Xfermode

data class MyPaint(
    var flag: Int = Paint.ANTI_ALIAS_FLAG,
    var color: Int = Color.BLACK,
    var strokeWidth: Float = 10F,
    var style: Paint.Style = Paint.Style.STROKE,
    var strokeJoin: Paint.Join = Paint.Join.ROUND,
    var strokeCap: Paint.Cap = Paint.Cap.ROUND,
    var ereaser: Boolean = false
) {
    fun toPaint(): Paint {
        return if(ereaser){
            val paintEraser = Paint()
            paintEraser.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            paintEraser
        }else{
            val paint = Paint()
            paint.flags = flag
            paint.color = color
            paint.strokeWidth = strokeWidth
            paint.style = style
            paint.strokeJoin = strokeJoin
            paint.strokeCap = strokeCap
            paint
        }
    }
}