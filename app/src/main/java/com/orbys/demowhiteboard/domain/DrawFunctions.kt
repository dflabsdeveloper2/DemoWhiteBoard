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
import com.orbys.demowhiteboard.domain.model.MyLines
import com.orbys.demowhiteboard.domain.model.ScaleResult
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
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

    fun rotateImage(imageSelected: ImageBitmapData, event: MotionEvent): ImageBitmapData {
        // Calcula el ángulo entre los dedos en grados
        val deltaX = event.getX(0) - event.getX(1)
        val deltaY = event.getY(0) - event.getY(1)
        val radians = atan2(deltaY.toDouble(), deltaX.toDouble())
        var degrees = Math.toDegrees(radians).toFloat()

        // Asegúrate de que el ángulo esté en el rango [0, 360)
        degrees = (degrees + 360) % 360

        // Rotación con tres o más dedos
        imageSelected.rotation = degrees

        // Restringe la rotación para evitar giros excesivos
        if (imageSelected.rotation < 0) {
            imageSelected.rotation += 360f
        }

        // Devuelve el objeto ImageBitmap2 rotado
        return imageSelected
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

    fun checkIfLineClosed(points: List<PointF>): RectF? {
        if (points.size < 2) {
            return null
        }

        val minX = points.minBy { it.x }.x ?: return null
        val maxX = points.maxBy { it.x }.x ?: return null
        val minY = points.minBy { it.y }.y ?: return null
        val maxY = points.maxBy { it.y }.y ?: return null

        // Verifica si el primer y último punto están cerca para determinar si la línea está cerrada
        val firstPoint = points[0]
        val lastPoint = points[points.size - 1]
        val distanceThreshold = 10f // Umbral de distancia para considerar que la línea está cerrada

        if (Math.abs(firstPoint.x - lastPoint.x) <= distanceThreshold &&
            Math.abs(firstPoint.y - lastPoint.y) <= distanceThreshold
        ) {
            return RectF(minX, minY, maxX, maxY)
        }

        return null
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
}
