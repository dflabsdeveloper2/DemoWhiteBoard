package com.orbys.demowhiteboard.ui.youtube

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.orbys.demowhiteboard.core.GlobalConfig
import com.orbys.demowhiteboard.domain.DrawFunctions
import com.orbys.demowhiteboard.ui.core.Helper
import com.orbys.demowhiteboard.ui.youtube.model.YoutubeVideo
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
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

    fun addYouTubePlayer(origin: YoutubeVideo) {
        if (youtubeVideos.size < GlobalConfig.numMaxYoutubePage) {
            val youTubePlayerView = initializeYouTubePlayerView(context,origin.id)

            Log.d("YOUTUBE", "add youtube")
            val video = origin.apply { viewer = youTubePlayerView }

            youtubeVideos.add(video)
            val layoutParams = LayoutParams(video.width, LayoutParams.WRAP_CONTENT)
            layoutParams.leftMargin = x.toInt()
            layoutParams.topMargin = y.toInt()
            addView(youTubePlayerView, layoutParams)

            // Añade el icono de borrar al YouTubePlayerView con los márgenes y tamaño deseados
            val deleteImageView = ImageView(context)
            deleteImageView.setImageResource(android.R.drawable.ic_delete)

            val deleteLayoutParams = LayoutParams(Helper.dpToPx(20,context), Helper.dpToPx(20,context))
            deleteLayoutParams.gravity = Gravity.END or Gravity.TOP  // Posiciona en la esquina superior derecha

            youTubePlayerView.addView(deleteImageView, deleteLayoutParams)
            deleteImageView.setOnClickListener {
                Log.d("ICONO", "borrar")
                removeYouTubePlayer(video)
            }
        }
    }

    fun addListYouTubeVideos(videoList: List<YoutubeVideo>) {
        youtubeVideos.clear()
        if (videoList.size < GlobalConfig.numMaxYoutubePage) {
            for (video in videoList) {
                if (video.viewer == null){
                    video.viewer = initializeYouTubePlayerView(context,video.id)
                }

                // Añade el video al FrameLayout
                val layoutParams = LayoutParams(video.width, video.height)
                layoutParams.leftMargin = video.x.toInt()
                layoutParams.topMargin = video.y.toInt()
                video.viewer?.let {
                    it.layoutParams = layoutParams
                    it.rotation = video.rotation
                    it.scaleX = video.scaleX.coerceIn(1f, 3f)
                    it.scaleY = video.scaleY.coerceIn(1f, 3f)

                    addView(it)
                }

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

        val layoutParams = video.viewer?.layoutParams as LayoutParams
        layoutParams.leftMargin = limitedX.toInt()
        layoutParams.topMargin = limitedY.toInt()
        video.viewer?.layoutParams = layoutParams
    }

    private fun removeYouTubePlayer(video: YoutubeVideo) {
        removeView(video.viewer)
        youtubeVideos.remove(video)
        selectedVideo = null

        invalidate()
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

    private var initialFingerSpacing = 1f
    private var initialScaleX = 1f
    private var initialScaleY = 1f

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isMoveModeEnabled) {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    lastX = event.x
                    lastY = event.y
                    selectedVideo = findSelectedVideo(event.x, event.y)
                    initialFingerSpacing = DrawFunctions.getFingerSpacing(event)

                    if (selectedVideo == null) return super.onTouchEvent(event)
                }

                MotionEvent.ACTION_MOVE -> {
                    selectedVideo?.let { video ->
                        if (event.pointerCount > 1) {
                            val newScale = DrawFunctions.scaleImage(
                                video.x,
                                video.y,
                                video.width.toFloat(),
                                video.height.toFloat(),
                                event,
                                initialFingerSpacing,
                                300f
                            )
                            newScale?.let {
                                video.x = it.x
                                video.y = it.y
                                video.width = it.width.toInt()
                                video.height = it.height.toInt()

                                val layoutParams = video.viewer?.layoutParams as LayoutParams
                                layoutParams.width = video.width
                                layoutParams.height = video.height
                                layoutParams.leftMargin = video.x.toInt()
                                layoutParams.topMargin = video.y.toInt()
                                video.viewer?.layoutParams = layoutParams

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
            if (video.viewer?.let { isViewContains(it, x, y) } == true) {
                return video
            }
        }
        return null
    }

    private fun isViewContains(view: View, x: Float, y: Float): Boolean {
        val location = IntArray(2)
        view.getLocationOnScreen(location)

        // Ajusta las coordenadas de los puntos táctiles de acuerdo con el escalado
        val scaledX = x / view.scaleX
        val scaledY = y / view.scaleY

        val rect = Rect(
            location[0],
            location[1],
            (location[0] + view.width * view.scaleX).toInt(),
            (location[1] + view.height * view.scaleY).toInt()
        )
        return rect.contains(scaledX.toInt(), scaledY.toInt())
    }

    private fun initializeYouTubePlayerView(context: Context,videoId: String): YouTubePlayerView {
        val playerView = YouTubePlayerView(context)
        playerView.enableAutomaticInitialization = false
        val listenner = object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                /* val defaultPlayerUiController =
                     DefaultPlayerUiController(playerView, youTubePlayer)
                 playerView.setCustomPlayerUi(defaultPlayerUiController.rootView)*/

                youTubePlayer.cueVideo(videoId, 0f)
            }
        }
        val options: IFramePlayerOptions =
            IFramePlayerOptions.Builder().controls(1).build()

        playerView.initialize(listenner,options)

        return playerView
    }
}