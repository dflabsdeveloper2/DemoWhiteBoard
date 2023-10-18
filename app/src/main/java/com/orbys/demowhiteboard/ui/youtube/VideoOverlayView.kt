package com.orbys.demowhiteboard.ui.youtube

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import com.orbys.demowhiteboard.ui.youtube.model.YoutubeVideo
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

class VideoOverlayView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    private val youtubeVideos: MutableList<YoutubeVideo> = mutableListOf()
    private var selectedVideo: YoutubeVideo? = null
    private var isMoveModeEnabled = false
    private var isScalingOrRotating = false
    private val marginThreshold = 10
    private var lastX = 0f
    private var lastY = 0f

    fun getYouTubeVideos(): List<YoutubeVideo> {
        return youtubeVideos.toList()
    }

    fun setMovementMode() {
        isMoveModeEnabled = true
    }

    fun setDrawingMode() {
        isMoveModeEnabled = false
    }

    fun addYouTubePlayer(youTubePlayerView: YouTubePlayerView, x: Float, y: Float) {
        if (youtubeVideos.size < 3) {
            Log.d("YOUTUBE", "add youtube")
            val video = YoutubeVideo(
                youTubePlayerView,
                x,
                y,
                600,
                400,
                0f,
                1f,
                1f
            )
            youtubeVideos.add(video)
            val layoutParams = LayoutParams(video.width, LayoutParams.WRAP_CONTENT)
            layoutParams.leftMargin = x.toInt()
            layoutParams.topMargin = y.toInt()
            addView(youTubePlayerView, layoutParams)
            //addView(youTubePlayerView, createLayoutParams(video))
        }
    }

    fun addListYouTubeVideos(videoList: List<YoutubeVideo>) {
        youtubeVideos.clear()
        if(videoList.size<3){
            for (video in videoList) {
                // Añade el video al FrameLayout
                val layoutParams = LayoutParams(video.width, video.height)
                layoutParams.leftMargin = video.x.toInt()
                layoutParams.topMargin = video.y.toInt()
                video.viewer.layoutParams = layoutParams

                video.viewer.rotation = video.rotation
                video.viewer.scaleX = video.scaleX
                video.viewer.scaleY = video.scaleY

                addView(video.viewer)
                youtubeVideos.add(video)
            }
        }
    }

    private fun moveYouTubePlayer(video: YoutubeVideo, newX: Float, newY: Float) {
        val maxX = width - video.width - marginThreshold
        val maxY =
            height - 300 - video.height - marginThreshold //TODO: revisar si el height es toda la pantalla
        val limitedX = newX.coerceIn(marginThreshold.toFloat(), maxX.toFloat())
        val limitedY = newY.coerceIn(marginThreshold.toFloat(), maxY.toFloat())

        video.x = limitedX
        video.y = limitedY

        val layoutParams = video.viewer.layoutParams as LayoutParams
        layoutParams.leftMargin = limitedX.toInt()
        layoutParams.topMargin = limitedY.toInt()
        video.viewer.layoutParams = layoutParams
    }

    fun removeYouTubePlayer(video: YoutubeVideo) {
        removeView(video.viewer)
        youtubeVideos.remove(video)
        selectedVideo = null
    }

    fun clearListYoutube() {
        youtubeVideos.clear()
        removeAllViews()
        selectedVideo = null
        setDrawingMode()
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return isMoveModeEnabled
    }

    private fun getFingerSpacing(event: MotionEvent): Float {
        if (event.pointerCount >= 2) {
            val x = event.getX(0) - event.getX(1)
            val y = event.getY(0) - event.getY(1)
            return kotlin.math.sqrt((x * x + y * y).toDouble()).toFloat()
        }
        return 1f
    }

    private var initialFingerSpacing = 1f
    private var initialScaleX = 1f
    private var initialScaleY = 1f

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isMoveModeEnabled) {
            Log.d("YOUTUBE", "pointer-> ${event.pointerCount}")
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    lastX = event.x
                    lastY = event.y
                    selectedVideo = findSelectedVideo(event.x, event.y)
                    initialFingerSpacing = getFingerSpacing(event)
                }

                MotionEvent.ACTION_MOVE -> {
                    if (event.pointerCount > 1) {
                        // Calcula la nueva distancia entre los dedos para escalar
                        val newSpacing = getFingerSpacing(event)
                        Log.d("YOUTUBE", "init finger space= $initialFingerSpacing")
                        Log.d("YOUTUBE", "finger space= $newSpacing")
                        // Calcula el factor de escala basado en las distancias inicial y actual
                        if (newSpacing > 0 && initialFingerSpacing > 0) {
                            var scaleFactor = newSpacing / initialFingerSpacing
                            Log.d("YOUTUBE", "scale factor= $scaleFactor")
                            // Aplica el escalado al video dentro de los límites mínimo y máximo
                            selectedVideo?.let { video ->
                                val scaleFactor = (newSpacing / (initialFingerSpacing*100)).coerceIn(1f, 3f)
                                val newScaleX = scaleFactor
                                val newScaleY = scaleFactor

                                video.scaleX = newScaleX
                                video.scaleY = newScaleY

                                Log.d("DEBUG", "YouTubePlayerView - Width: ${video.width}, Height: ${video.height}, X: ${video.x}, Y: ${video.y}")

                                // Ajusta las coordenadas x y y para mantener la posición visualmente constante después del escalado
                                val deltaX = video.x * (video.scaleX / initialScaleX - 1)
                                val deltaY = video.y * (video.scaleY / initialScaleY - 1)
                                video.x -= deltaX
                                video.y -= deltaY

                                Log.d("YOUTUBE", "X ${video.x} Y ${video.y}")
                                val layoutParams = video.viewer.layoutParams as LayoutParams
                                layoutParams.width = (video.width * video.scaleX).toInt()
                                layoutParams.height = (video.height * video.scaleY).toInt()
                                Log.d(
                                    "YOUTUBE",
                                    "layoutParams X ${layoutParams.width} Y ${layoutParams.height}"
                                )
                                video.viewer.layoutParams = layoutParams
                            }
                        }
                    } else {
                        selectedVideo?.let { video ->
                            val deltaX = event.x - lastX
                            val deltaY = event.y - lastY

                            val newX = video.x + deltaX
                            val newY = video.y + deltaY

                            moveYouTubePlayer(video, newX, newY)

                            lastX = event.x
                            lastY = event.y
                        }
                    }
                }

                MotionEvent.ACTION_UP -> {
                    initialFingerSpacing = 1f
                    initialScaleX = 1f
                    initialScaleY = 1f
                    selectedVideo = null
                }
            }

            return true
        }
        return super.onTouchEvent(event)
    }

    private fun findSelectedVideo(x: Float, y: Float): YoutubeVideo? {
        for (video in youtubeVideos) {
            if (isViewContains(video.viewer, x.toInt(), y.toInt())) {
                return video
            }
        }
        return null
    }

    private fun isViewContains(view: View, x: Int, y: Int): Boolean {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val rect =
            Rect(location[0], location[1], location[0] + view.width, location[1] + view.height)
        return rect.contains(x, y)
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        private var prevX: Float = 0f
        private var prevY: Float = 0f
        private val rotationSpeed: Float = 0.5f

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            if (isScalingOrRotating) return false

            selectedVideo?.let { video ->
                if (e2.pointerCount >= 3 && e1 != null) {
                    // Calcula la dirección del movimiento (izquierda o derecha) y aplica la rotación en consecuencia
                    val deltaX = e2.x - prevX
                    if (deltaX > 0) {
                        // Movimiento hacia la derecha
                        video.rotation += rotationSpeed
                    } else if (deltaX < 0) {
                        // Movimiento hacia la izquierda
                        video.rotation -= rotationSpeed
                    }

                    video.viewer.rotation = video.rotation
                    video.viewer.scaleX = video.scaleX
                    video.viewer.scaleY = video.scaleY
                }
            }
            // Actualiza los valores previos para el siguiente movimiento
            prevX = e2.x
            prevY = e2.y
            return true
        }
    }
}