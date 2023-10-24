package com.orbys.demowhiteboard.ui.core

import android.view.MotionEvent
import com.orbys.demowhiteboard.core.GlobalConfig
import com.orbys.demowhiteboard.domain.model.ImageBitmap2
import kotlin.math.atan2

data class ScaleResult(val x:Float,val y:Float,val width: Float,val height: Float)

object DrawFunctions {

    fun getFingerSpacing(event: MotionEvent): Float {
        if (event.pointerCount == 2) {
            val x = event.getX(0) - event.getX(1)
            val y = event.getY(0) - event.getY(1)
            return kotlin.math.sqrt((x * x + y * y).toDouble()).toFloat()
        }
        return 1f
    }

    fun scaleImage(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        event: MotionEvent,
        initialFingerSpacing: Float, minWidth:Float = 100f
    ): ScaleResult? {

        val newSpacing = getFingerSpacing(event)

        // Calcula el factor de escala basado en las distancias inicial y actual
        if (newSpacing > 0 && initialFingerSpacing > 0) {
            // Calcula el tamaño actual de la imagen

            // Calcula el nuevo ancho y alto utilizando los límites mínimo y máximo
            val maxWidth = 2000f

            var newWidth = (width * (newSpacing / initialFingerSpacing)/100).coerceIn(minWidth, maxWidth)
            var newHeight = (height * (newSpacing / initialFingerSpacing)/100).coerceIn(minWidth, maxWidth)

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

    fun rotateImage(imageSelected: ImageBitmap2, event: MotionEvent): ImageBitmap2 {
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
}