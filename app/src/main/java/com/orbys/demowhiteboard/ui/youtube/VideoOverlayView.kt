package com.orbys.demowhiteboard.ui.youtube

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.FrameLayout
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlin.math.atan2

class VideoOverlayView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    private val youtubePlayers: MutableList<YouTubePlayerView> = mutableListOf()
    private var selectedPlayer: YouTubePlayerView? = null
    private var isMoveModeEnabled = false
    private val marginThreshold = 10

    fun setMovementMode() {
        isMoveModeEnabled = true
    }

    fun setDrawingMode() {
        isMoveModeEnabled = false
    }

    fun addYouTubePlayer(youTubePlayerView: YouTubePlayerView, x: Float, y: Float) {
        if (youtubePlayers.size < 5) {
            val layoutParams = LayoutParams(600, LayoutParams.WRAP_CONTENT)
            layoutParams.leftMargin = x.toInt()
            layoutParams.topMargin = y.toInt()
            addView(youTubePlayerView, layoutParams)
            youtubePlayers.add(youTubePlayerView)

        }
        Log.d("YOUTUBE", "youtubePlayers add")
    }

    private fun moveYouTubePlayer(youTubePlayerView: YouTubePlayerView, x: Float, y: Float) {
        val layoutParams = youTubePlayerView.layoutParams as LayoutParams
        layoutParams.leftMargin = x.toInt()
        layoutParams.topMargin = y.toInt()
        youTubePlayerView.layoutParams = layoutParams
    }

    fun removeYouTubePlayer(youTubePlayerView: YouTubePlayerView) {
        removeView(youTubePlayerView)
        youtubePlayers.remove(youTubePlayerView)
        selectedPlayer = null
    }

    private var lastX = 0f
    private var lastY = 0f
    private var scaleFactor = 1.0f
    private var lastScaleFactor = 1.0f

    private val scaleListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor

            // Limita el escalado dentro de ciertos límites (ajústalo según sea necesario)
            scaleFactor = scaleFactor.coerceIn(0.1f, 5.0f)

            // Calcula el cambio en el factor de escala
            val deltaScale = scaleFactor / lastScaleFactor

            // Aplica el cambio de escala solo a la vista seleccionada
            selectedPlayer?.let { player ->
                player.scaleX *= deltaScale
                player.scaleY *= deltaScale
            }

            lastScaleFactor = scaleFactor
            return true
        }

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            Log.d("YOUTUBE", "INICIO gesto pellizcar")
            lastScaleFactor = scaleFactor
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            Log.d("YOUTUBE", "FIN gesto pellizcar")
        }
    }
    private val rotateListener = object : GestureDetector.SimpleOnGestureListener() {
        private var initialAngle = 0f
        private val rotationScaleFactor = 0.5f // Puedes ajustar este valor según sea necesario


        override fun onDown(e: MotionEvent): Boolean {
            youtubePlayers.forEach { player ->
                if (isViewContains(player, e.x.toInt(), e.y.toInt())) {
                    selectedPlayer = player
                    initialAngle =
                        Math.toDegrees(
                            atan2(
                                (e.y - player.y).toDouble(),
                                (e.x - player.x).toDouble()
                            )
                        )
                            .toFloat()
                    return@forEach
                }
            }
            return super.onDown(e)
        }

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            selectedPlayer?.let { player ->
                if (e2.pointerCount >= 3 && e1 != null) {
                    // Calcula el ángulo actual del toque
                    val currentAngle =
                        Math.toDegrees(
                            atan2(
                                (e2.y - player.y).toDouble(),
                                (e2.x - player.x).toDouble()
                            )
                        )
                            .toFloat()

                    // Calcula la diferencia de ángulo
                    var deltaAngle = currentAngle - initialAngle

                    // Ajusta la diferencia de ángulo para evitar saltos cuando se completa una vuelta
                    if (deltaAngle > 180) {
                        deltaAngle -= 360
                    } else if (deltaAngle < -180) {
                        deltaAngle += 360
                    }

                    // Aplica el factor de escala para controlar la velocidad de rotación
                    deltaAngle *= rotationScaleFactor

                    // Aplica la rotación a la vista seleccionada
                    player.rotation += deltaAngle

                    // Actualiza el ángulo inicial para el próximo movimiento
                    initialAngle = currentAngle
                }
            }
            return super.onScroll(e1, e2, distanceX, distanceY)
        }
    }

    private val mScaleDetector = ScaleGestureDetector(context, scaleListener)
    private val rotateDetector = GestureDetector(context, rotateListener)


    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return isMoveModeEnabled
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isMoveModeEnabled) {
            mScaleDetector.onTouchEvent(event)
            rotateDetector.onTouchEvent(event)
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    // Se inicia un nuevo toque, guarda las coordenadas iniciales
                    lastX = event.x
                    lastY = event.y
                }

                MotionEvent.ACTION_MOVE -> {
                    // Se detecta un movimiento, calcula la diferencia entre las coordenadas actuales y las anteriores
                    val deltaX = event.x - lastX
                    val deltaY = event.y - lastY

                    // Mueve el YouTubePlayerView según las diferencias de coordenadas
                    youtubePlayers.forEach { player ->
                        if (isViewContains(player, event.x.toInt(), event.y.toInt())) {
                            val newX = player.x + deltaX
                            val newY = player.y + deltaY

                            // Limita el movimiento dentro de los límites del FrameLayout
                            val maxX = width - player.width - marginThreshold
                            val maxY = height - player.height - marginThreshold
                            val limitedX = newX.coerceIn(marginThreshold.toFloat(), maxX.toFloat())
                            val limitedY = newY.coerceIn(marginThreshold.toFloat(), maxY.toFloat())

                            moveYouTubePlayer(player, limitedX, limitedY)
                        }
                    }

                    // Actualiza las coordenadas anteriores con las actuales para el próximo movimiento
                    lastX = event.x
                    lastY = event.y
                }

                MotionEvent.ACTION_UP -> {
                    selectedPlayer = null
                }
            }
            return true
        }
        // Llama a onTouchEvent de la superclase para asegurar el funcionamiento normal del evento táctil
        return super.onTouchEvent(event)
    }

    // Verifica si un punto (x, y) está contenido dentro de una vista
    private fun isViewContains(view: View, x: Int, y: Int): Boolean {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val rect =
            Rect(location[0], location[1], location[0] + view.width, location[1] + view.height)
        return rect.contains(x, y)
    }
}