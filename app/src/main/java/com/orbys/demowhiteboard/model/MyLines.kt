package com.orbys.demowhiteboard.model

import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF

data class MyLines(
    var listLines: List<MyLine>,
    var linesEreaser: List<MyLineEraser>,
    var background: Int,
    var video: Int,
    var page: Int
)

data class MyLine(var line: List<PointF>, val props: Paint)
data class MyLineEraser(var line: List<RectF?>, val props: Paint)
