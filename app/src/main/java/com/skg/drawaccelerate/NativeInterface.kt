package com.skg.drawaccelerate

import android.graphics.Bitmap
import android.util.Log

object NativeInterface {
    init {
        try {
            System.loadLibrary("sketch-accelerate")
        }catch (e:UnsatisfiedLinkError){
            Log.d("ACCELERATE","Error al cargar la libreria")
        }
    }

    // Inicializar aceleración de nivel inferior, devuelve 0 si es exitoso, o un valor diferente si falla
    external fun accelerateInit(): Int

    // Liberar aceleración de nivel inferior
    external fun accelerateDeinit()

    // Vincular evento de ciclo de vida de la actividad
    external fun notifyLifecycleEvent(stage: Int)

    external fun createPaint(id: Int, color: Int, width: Float)

    external fun deletePaint(id: Int)

    /**
     * El proceso de aceleración de una sola línea se realiza de la siguiente manera:
     * Cuando se toca el primer dedo, se utiliza #drawStart junto con #createPaint #drawDown para establecer el identificador (id) y configurar el ancho y el color del trazo.
     * Cuando se tocan otros dedos posteriores, se continúa llamando a #createPaint #drawDown para establecer el identificador (id) y configurar el ancho y el color del trazo.
     * Cuando se mueve el trazo, se utilizan los parámetros de #drawPoints, que incluyen el identificador (id) y una serie de puntos (punto1x, punto1y, punto2x, punto2y,...).
     * Al finalizar el trazo, se llama a #drawUp.
     * Cuando se levanta el último dedo, se completa la operación llamando a #drawFinish y se espera a que la capa de Android complete el dibujo antes de llamar a syncLayer para borrar el contenido mostrado en la capa inferior.
     */

    external fun drawStart()
    external fun drawDown(id: Int, x: Float, y: Float)
    external fun drawPoints(id: Int, points: FloatArray?)
    external fun drawUp(id: Int, x: Float, y: Float)
    external fun drawFinish()
    external fun eraserStart(x: Float, y: Float, size_w: Float, size_h: Float, bg: Bitmap?)
    external fun eraserMove(x: Float, y: Float, bg: Bitmap?)
    external fun eraserStop(bg: Bitmap?)

    // clean accelerate layer
    external fun syncLayer()
}