package com.orbys.demowhiteboard.domain

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import android.view.MotionEvent
import com.orbys.demowhiteboard.core.GlobalConfig
import com.orbys.demowhiteboard.domain.model.ImageBitmap
import com.orbys.demowhiteboard.domain.model.ImageBitmapData
import com.orbys.demowhiteboard.domain.model.ImageTransformResult
import com.orbys.demowhiteboard.domain.model.ImageTransformResultData
import com.orbys.demowhiteboard.domain.model.MyLine
import com.orbys.demowhiteboard.domain.model.MyLines
import com.orbys.demowhiteboard.domain.model.ScaleResult
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt


object DrawFunctions {

    fun getFingerSpacing(event: MotionEvent): Float {
        if (event.pointerCount >= 2) {
            val xDistances = mutableListOf<Float>()
            val yDistances = mutableListOf<Float>()

            // Calcula las distancias en el eje X y Y entre todos los puntos
            for (i in 0 until event.pointerCount) {
                for (j in i + 1 until event.pointerCount) {
                    val xDistance = abs(event.getX(i) - event.getX(j))
                    val yDistance = abs(event.getY(i) - event.getY(j))
                    xDistances.add(xDistance)
                    yDistances.add(yDistance)
                }
            }

            // Encuentra la distancia máxima en el eje X y Y
            val maxXDistance = xDistances.maxOrNull() ?: 0f
            val maxYDistance = yDistances.maxOrNull() ?: 0f

            // Calcula la distancia entre los puntos usando el teorema de Pitágoras
            return sqrt((maxXDistance * maxXDistance + maxYDistance * maxYDistance).toDouble()).toFloat()
        }
        return 1f
    }

    fun scaleImage(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        event: MotionEvent,
        initialFingerSpacing: Float, minWidth: Float = 200f
    ): ScaleResult? {

        val newSpacing = getFingerSpacing(event)

        // Calcula el factor de escala basado en las distancias inicial y actual
        if (newSpacing > 0 && initialFingerSpacing > 0) {
            // Calcula el tamaño actual de la imagen

            // Calcula el nuevo ancho y alto utilizando los límites mínimo y máximo
            val maxWidth = 2000f

            var newWidth =
                (width * (newSpacing / initialFingerSpacing) / 100).coerceIn(minWidth, maxWidth)
            var newHeight =
                (height * (newSpacing / initialFingerSpacing) / 100).coerceIn(minWidth, maxWidth)

            // Ajusta el nuevo ancho y alto para mantener la proporción de la imagen
            if (width > height) {
                val ratio = height / width
                newHeight = newWidth * ratio
            } else {
                val ratio = width / height
                newWidth = newHeight * ratio
            }

            // Calcula las nuevas coordenadas del objeto para mantenerlo centrado
            val newLeft = x - (newWidth / 2) + (width / 2)
            val newTop = y - (newHeight / 2) + (height / 2)

            // Verifica si el objeto se sale de la pantalla y ajusta las coordenadas si es necesario
            val screenWidth = GlobalConfig.SCREEN_WIDTH
            val screenHeight = GlobalConfig.SCREEN_HEIGHT

            return if (newLeft >= 0 && newLeft + newWidth <= screenWidth &&
                newTop >= 0 && newTop + newHeight <= screenHeight
            ) {
                ScaleResult(newLeft, newTop, newWidth, newHeight)
            } else {
                null
            }
        }

        return null
    }

    fun calculateRotation(event: MotionEvent): Float {
        if (event.pointerCount >= 2) {
            val points = mutableListOf<Pair<Float, Float>>()

            // Almacena las coordenadas de los puntos en una lista
            for (i in 0 until event.pointerCount) {
                points.add(event.getX(i) to event.getY(i))
            }

            // Calcula los ángulos entre los puntos usando la función atan2
            var totalAngle = 0.0
            for (i in 0 until points.size - 1) {
                val angle = atan2(
                    points[i + 1].second - points[i].second,
                    points[i + 1].first - points[i].first
                )
                totalAngle += angle
            }

            // Calcula el ángulo de rotación promedio de los ángulos calculados
            val averageAngle = totalAngle / (points.size - 1)

            // Convierte el ángulo a grados y asegúrate de que esté en el rango [0, 360)
            var degrees = Math.toDegrees(averageAngle).toFloat()
            degrees = (degrees + 360) % 360

           /* // Ajusta el ángulo para que sea positivo en sentido horario y negativo en sentido antihorario
            degrees = if (degrees > 180) degrees - 360 else degrees*/

            return degrees
        }
        return 0f
    }

    fun normalizeAngle(angle: Int): Int {
        var normalizedAngle = angle
        while (normalizedAngle < 0) {
            normalizedAngle += 360
        }
        return normalizedAngle % 360
    }

    fun scaleRotateTranslateBitmap(myImage: ImageBitmapData): ImageTransformResult {
        // Crear una matriz para la rotación
        val rotationMatrix = Matrix()
        rotationMatrix.postRotate(myImage.rotation)

        // Escalar el bitmap al tamaño deseado
        val scaledWidth = myImage.width
        val scaledHeight = myImage.height

        // Aplicar la traslación
        val translationMatrix = Matrix()
        translationMatrix.postTranslate(myImage.x, myImage.y)

        // Combina las matrices de rotación y escala
        val combinedMatrix = Matrix()
        combinedMatrix.setConcat(rotationMatrix, translationMatrix)

        // Crear un bitmap rotado, escalado y trasladado
        val transformedBitmap = Bitmap.createBitmap(
            myImage.image,
            0,
            0,
            myImage.image.width,
            myImage.image.height,
            combinedMatrix,
            true
        )

        // Calcular el RectF después de las transformaciones
        val rectF = RectF(
            myImage.x,
            myImage.y,
            myImage.x + scaledWidth,
            myImage.y + scaledHeight
        )

        return ImageTransformResult(transformedBitmap, rectF)
    }

    fun rotateLines(lines: List<MyLines>, angleDegrees: Float, range: RectF): List<MyLines> {
        val pivotX = (range.left + range.right) / 2
        val pivotY = (range.top + range.bottom) / 2

        val updatedLines = lines.map { myLines ->
            val updatedListLines = myLines.listLines.map { myLine ->
                val updatedLine = myLine.line?.map { point ->
                    if (point.x >= range.left && point.x <= range.right &&
                        point.y >= range.top && point.y <= range.bottom
                    ) {
                        val rotatedPoint =
                            rotatePoint(point.x, point.y, angleDegrees, pivotX, pivotY)
                        PointF(rotatedPoint.first, rotatedPoint.second)
                    } else {
                        point
                    }
                }
                myLine.copy(line = updatedLine)
            }
            myLines.copy(listLines = updatedListLines)
        }

        return updatedLines
    }

    fun scaleLines(lines: List<MyLines>, scaleFactor: Float, range: RectF): List<MyLines> {
        val pivotX = (range.left + range.right) / 2
        val pivotY = (range.top + range.bottom) / 2

        val updatedLines = lines.map { myLines ->
            val updatedListLines = myLines.listLines.map { myLine ->
                val updatedLine = myLine.line?.map { point ->
                    if (point.x >= range.left && point.x <= range.right &&
                        point.y >= range.top && point.y <= range.bottom
                    ) {
                        val scaledPoint = scalePoint(point.x, point.y, scaleFactor, pivotX, pivotY)
                        PointF(scaledPoint.first, scaledPoint.second)
                    } else {
                        point
                    }
                }
                myLine.copy(line = updatedLine)
            }
            myLines.copy(listLines = updatedListLines)
        }

        return updatedLines
    }

    fun translateLines(
        lines: List<MyLines>,
        translationX: Float,
        translationY: Float,
        range: RectF
    ): List<MyLines> {
        val updatedLines = lines.map { myLines ->
            val updatedListLines = myLines.listLines.map { myLine ->
                val updatedLine = myLine.line?.map { point ->
                    if (point.x >= range.left && point.x <= range.right &&
                        point.y >= range.top && point.y <= range.bottom
                    ) {
                        point.apply {
                            x += translationX
                            y += translationY
                        }
                    } else {
                        point
                    }
                }
                myLine.copy(line = updatedLine)
            }
            myLines.copy(listLines = updatedListLines)
        }

        return updatedLines
    }

    private fun rotatePoint(
        x: Float,
        y: Float,
        angleDegrees: Float,
        pivotX: Float,
        pivotY: Float
    ): Pair<Float, Float> {
        val angleRadians = Math.toRadians(angleDegrees.toDouble())
        val cos = cos(angleRadians)
        val sin = sin(angleRadians)

        val translatedX = x - pivotX
        val translatedY = y - pivotY

        val rotatedX = translatedX * cos - translatedY * sin + pivotX
        val rotatedY = translatedX * sin + translatedY * cos + pivotY

        return Pair(rotatedX.toFloat(), rotatedY.toFloat())
    }

    private fun scalePoint(
        x: Float,
        y: Float,
        scaleFactor: Float,
        pivotX: Float,
        pivotY: Float
    ): Pair<Float, Float> {
        val scaledX = (x - pivotX) * scaleFactor + pivotX
        val scaledY = (y - pivotY) * scaleFactor + pivotY
        return Pair(scaledX, scaledY)
    }

    fun checkIfLineClosed(points: List<PointF>): Boolean {
        if (points.size < 3) {
            return false // Un polígono debe tener al menos 3 puntos
        }

        val lineSegments = mutableListOf<Pair<PointF, PointF>>()

        // Crear segmentos de línea a partir de los puntos
        for (i in 0 until points.size - 1) {
            val segmentStart = points[i]
            val segmentEnd = points[i + 1]
            lineSegments.add(Pair(segmentStart, segmentEnd))
        }

        // Verificar intersecciones entre segmentos de línea
        for (i in 0 until lineSegments.size - 1) {
            val segment1 = lineSegments[i]
            for (j in i + 1 until lineSegments.size) {
                val segment2 = lineSegments[j]
                if (doSegmentsIntersect(segment1.first, segment1.second, segment2.first, segment2.second)) {
                    return true // Hay una intersección, el polígono está cerrado
                }
            }
        }

        return false // No hay intersecciones, el polígono no está cerrado
    }

    fun getImageAndRect(data: ImageBitmap): ImageTransformResultData {
        val x = 50 // Posición inicial X
        val y = 50 // Posición Y fija para todos los bitmaps

        val originalWidth = data.image.width.toFloat()
        val originalHeight = data.image.height.toFloat()

        val maxWidth = 1000f // Ancho máximo permitido
        val maxHeight = 1000f // Altura máxima permitida

        // Calcula las proporciones para ajustar el ancho y el alto del bitmap
        val ratio = originalWidth / originalHeight
        var newWidth = if (originalWidth > maxWidth) maxWidth else originalWidth
        var newHeight = newWidth / ratio

        // Verifica si la altura supera el límite permitido
        if (newHeight > maxHeight) {
            val scaleRatio = maxHeight / newHeight
            newHeight *= scaleRatio
            newWidth *= scaleRatio
        }

        // Crea el rectángulo de destino con la posición X actual, posición Y y tamaño del bitmap ajustado
        val dstRect = RectF(
            x.toFloat(),
            y.toFloat(),
            x + newWidth,
            y + newHeight
        )


        val myImage = ImageBitmapData(
            data.image, x.toFloat(), y.toFloat(),
            newWidth, newHeight
        )

        return ImageTransformResultData(myImage,dstRect)
    }

    fun calculateRectFFromPoints(points: List<PointF>): RectF {
        if (points.isEmpty()) {
            // Si la lista está vacía, devuelve un RectF vacío
            return RectF()
        }

        // Inicializa los límites del RectF con los valores del primer punto
        var left = points[0].x
        var top = points[0].y
        var right = points[0].x
        var bottom = points[0].y

        // Encuentra los límites del RectF
        for (point in points) {
            left = min(left, point.x)
            top = min(top, point.y)
            right = max(right, point.x)
            bottom = max(bottom, point.y)
        }

        // Crea y devuelve el RectF con los límites encontrados
        return RectF(left, top, right, bottom)
    }

    fun getListLinesSelected(listPoints: List<PointF>): List<MyLine> {
        val newList = mutableListOf<MyLine>()
        val list = GlobalConfig.listMyWhiteBoard?.lines

        if (list.isNullOrEmpty()) return newList

        if(checkIfLineClosed(listPoints)) {
            for (whiteboard in list) {
                for (myLine in whiteboard.listLines) {
                    if (!myLine.line.isNullOrEmpty()) {
                        if (doLinesIntersect(myLine.line!!, listPoints) || myLine.line!!.all { pointIsInsidePolygon(it, listPoints) }) {
                            // Crea una nueva instancia de MyLine con las líneas intersectadas y agrega a newList
                            val intersectedLine = MyLine(
                                myLine.line,
                                myLine.lineEraser,
                                myLine.props,
                                myLine.imageBitmap
                            )
                            newList.add(intersectedLine)
                        }
                    }
                }
            }
        }

        return newList
    }

    private fun pointIsInsidePolygon(point: PointF, polygon: List<PointF>): Boolean {
        var crossings = 0
        for (i in 0 until polygon.size - 1) {
            val a = polygon[i]
            val b = polygon[i + 1]
            if (point.y > Math.min(a.y, b.y) &&
                point.y <= Math.max(a.y, b.y) &&
                point.x <= Math.max(a.x, b.x) &&
                a.y != b.y
            ) {
                val xIntersection = (point.y - a.y) * (b.x - a.x) / (b.y - a.y) + a.x
                if (a.x == b.x || point.x <= xIntersection) {
                    crossings++
                }
            }
        }
        // Si hay un número impar de cruces, el punto está dentro del polígono
        return crossings % 2 != 0
    }

    private fun doLinesIntersect(line1: List<PointF>, line2: List<PointF>): Boolean {
        for (i in 1 until line1.size) {
            val p1 = line1[i - 1]
            val p2 = line1[i]
            for (j in 1 until line2.size) {
                val p3 = line2[j - 1]
                val p4 = line2[j]
                if (doSegmentsIntersect(p1, p2, p3, p4)) {
                    return true
                }
            }
        }
        return false
    }

    private fun doSegmentsIntersect(p1: PointF, p2: PointF, p3: PointF, p4: PointF): Boolean {
        val s1_x = p2.x - p1.x
        val s1_y = p2.y - p1.y
        val s2_x = p4.x - p3.x
        val s2_y = p4.y - p3.y

        val s = (-s1_y * (p1.x - p3.x) + s1_x * (p1.y - p3.y)) / (-s2_x * s1_y + s1_x * s2_y)
        val t = (s2_x * (p1.y - p3.y) - s2_y * (p1.x - p3.x)) / (-s2_x * s1_y + s1_x * s2_y)

        return s in 0.0..1.0 && t in 0.0..1.0
    }

    fun rotateRectAndPoints(rectF: RectF, pointsInsideRect: List<PointF>, degrees: Float): Pair<RectF, List<PointF>> {
        // Crear una matriz de rotación
        val matrix = Matrix()
        matrix.setRotate(degrees, rectF.centerX(), rectF.centerY())

        // Rotar el RectF
        val rotatedRectF = RectF(rectF)
        matrix.mapRect(rotatedRectF)

        // Rotar los puntos
        val rotatedPoints = pointsInsideRect.map { point ->
            val tempPoint = FloatArray(2)
            tempPoint[0] = point.x
            tempPoint[1] = point.y
            matrix.mapPoints(tempPoint)
            PointF(tempPoint[0], tempPoint[1])
        }

        return Pair(rotatedRectF, rotatedPoints)
    }
}
