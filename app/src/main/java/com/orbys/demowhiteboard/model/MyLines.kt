package com.orbys.demowhiteboard.model

import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF

data class MyLines(
    var listLines: List<MyLine>,
    var backgroundWallpaper:String?,
    var backgroundColor: Int,
    var video: Int,
    var page: Int
)

data class MyLine(var line: List<PointF>?,var lineEraser: List<RectF?>?, val props: MyPaint){
    fun toPath():Path?{
        if(line!=null) {
            val path = Path()


            if (line!!.isEmpty()) {
                return path
            }

            // Move to the first point
            path.moveTo(line!![0].x, line!![0].y)

            for (i in 1 until line!!.size) {
                // Add a line segment to the next point
                path.lineTo(line!![i].x, line!![i].y)
            }

            return path
        }
        return null
    }
}