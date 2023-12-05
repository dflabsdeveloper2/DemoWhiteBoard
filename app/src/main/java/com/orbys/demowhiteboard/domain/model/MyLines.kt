package com.orbys.demowhiteboard.domain.model

import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import com.orbys.demowhiteboard.ui.view.YoutubeVideo


data class MyWhiteboard(var lines: MutableList<MyLines>) {
    fun getAllImageBitmap(): List<ImageBitmapData> {
        return lines.flatMap { myLines -> myLines.listLines }
            .mapNotNull { myLine -> myLine.imageBitmap }
    }

    fun getAllPointsLine(): List<List<PointF>>{
        return lines.flatMap { myLines -> myLines.listLines }
            .mapNotNull { myLine -> myLine.line }
    }

    fun groupByPage(): List<MyLines> {
        val groupedLines = lines.groupBy { it.page }
        val result = mutableListOf<MyLines>()

        for ((page, lines) in groupedLines) {
            val allLinesPerPage:List<MyLine> = lines.flatMap { it.listLines }
            val backgroundWallpaper = lines.lastOrNull()?.backgroundWallpaper
            val backgroundColor = lines.lastOrNull()?.backgroundColor
            val youtubeList = lines.flatMap { it.listYoutube }

            val myLine = MyLines(allLinesPerPage, backgroundWallpaper, backgroundColor, youtubeList, page)
            result.add(myLine)
        }

        return result
    }
}

data class MyLines(
    var listLines: List<MyLine>,
    var backgroundWallpaper:String?,
    var backgroundColor: Int?,
    var listYoutube: List<YoutubeVideo>,
    var page: Int,
)

data class MyLine(var line: List<PointF>?,var lineEraser: List<RectF?>?, val props: MyPaint?, var imageBitmap: ImageBitmapData?){
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