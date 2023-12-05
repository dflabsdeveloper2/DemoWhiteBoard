package com.orbys.demowhiteboard.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.orbys.demowhiteboard.R
import com.orbys.demowhiteboard.core.GlobalConfig
import com.orbys.demowhiteboard.domain.DrawFunctions
import com.orbys.demowhiteboard.domain.model.ImageBitmapData
import com.orbys.demowhiteboard.domain.model.MyLine
import com.orbys.demowhiteboard.domain.model.MyLines
import com.orbys.demowhiteboard.ui.MainActivity
import com.orbys.demowhiteboard.ui.core.Helper
import com.orbys.demowhiteboard.ui.whiteboard.WriteBoard
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

class OverlayView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    private var isMoveModeEnabled = false
    private val marginThreshold = 10f
    private var selectedImage: ImageDataView? = null
    private var imageView: ViewImage? = null
    private var youtubeView: ViewYoutube? = null
    private var selectedYoutube: YoutubeVideo? = null
    private val youtubeVideos: MutableList<YoutubeVideo> = mutableListOf()
    private var lastX = 0f
    private var lastY = 0f
    private var rotateImage = 0f
    private val mainActivityContext: MainActivity? = context as? MainActivity

    //TODO: Falta eliminar imagen de la lista en main activity

    fun setMovementMode() {
        isMoveModeEnabled = true
    }

    fun setDrawingMode() {
        isMoveModeEnabled = false
        imageView?.let {
            removeView(it)
        }

        youtubeView?.let {
            removeAllViews()
        }

        selectedImage = null
        selectedYoutube = null
    }

    fun getYouTubeVideos(): List<YoutubeVideo> {
        return youtubeVideos.toList()
    }

    fun addYouTubePlayer(origin: YoutubeVideo) {
        if (youtubeVideos.size < GlobalConfig.numMaxYoutubePage) {
            val youTubePlayerView = initializeYouTubePlayerView(context, origin.id)

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

            val deleteLayoutParams =
                LayoutParams(Helper.dpToPx(20, context), Helper.dpToPx(20, context))
            deleteLayoutParams.gravity =
                Gravity.END or Gravity.TOP  // Posiciona en la esquina superior derecha

            youTubePlayerView.addView(deleteImageView, deleteLayoutParams)
            deleteImageView.setOnClickListener {
                Log.d("ICONO", "borrar")
                removeYouTubePlayer(video)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun addImageFloating(imageBitmap: ImageBitmapData) {

        if (selectedImage != null) return

        val layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)

        imageView = ViewImage(context)

        imageView?.let { viewImage ->
            // Agregar imageView a la vista OverlayView
            addView(viewImage, layoutParams)

            selectedImage = ImageDataView(viewImage, imageBitmap)

            rotateImage = 0f

            // Configurar la imagen en el imageView
            val mainImage = viewImage.findViewById<ImageView>(R.id.ivMainImage)
            mainImage.setImageBitmap(resizeImage(imageBitmap.image))

            val btnClose = viewImage.findViewById<ImageView>(R.id.ivCloseMainImage)
            val btnOk = viewImage.findViewById<ImageView>(R.id.ivOkMainImage)
            val btnRotation = viewImage.findViewById<ImageView>(R.id.ivItemRotationMainImagen)

            var lastRotationX = 0f
            var lastRotationY = 0f

            btnRotation.setOnTouchListener { view, motionEvent ->
                val centerX = mainImage.width / 2f
                val centerY = mainImage.height / 2f

                when (motionEvent.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        lastRotationY = motionEvent.x
                        lastRotationX = motionEvent.y
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val deltaX = motionEvent.x - lastRotationY

                        val rotationAngle = when {
                            deltaX > 0 -> 5f // Rotar hacia la derecha
                            deltaX < 0 -> -5f // Rotar hacia la izquierda
                            else -> 0f // No se realiza rotación si no hay movimiento horizontal
                        }

                        mainImage.pivotX = centerX
                        mainImage.pivotY = centerY

                        viewImage.rotation += rotationAngle
                        rotateImage = viewImage.rotation

                        lastRotationX = motionEvent.x
                        lastRotationY = motionEvent.y
                    }
                }
                true
            }

            btnClose.setOnClickListener {
                deleteImage()
            }

            btnOk.setOnClickListener {
                setNewPositionImage()
            }
        }
    }

    //TODO:REVISAR si es necesario
     override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        Log.d("OVERLAYVIEW","isModeMove: $isMoveModeEnabled youtube: ${selectedYoutube==null} image:${selectedImage == null}")
         return isMoveModeEnabled && selectedImage == null
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
                    val selectImageBitmap = findSelectedImage(event.x, event.y)
                    initialFingerSpacing = DrawFunctions.getFingerSpacing(event)
                    selectedYoutube = findSelectedVideo(event.x, event.y)
                    Log.d(
                        "OVERLAYVIEW",
                        "selectedYoutube -> $selectedYoutube"
                    )
                    if (selectImageBitmap != null || selectedYoutube!=null) {
                        if (selectedImage == null) {
                            Log.d("OVERLAYVIEW", "segunda")
                            if (selectImageBitmap != null) {
                                addImageFloating(selectImageBitmap)
                            }
                        }
                    } else {
                        if (selectedImage == null) {
                            Log.d("OVERLAYVIEW", "no encontrada imagen y video")
                            return super.onTouchEvent(event)
                        }
                    }
                }

                MotionEvent.ACTION_MOVE -> {
                    Log.d("OVERLAYVIEW", "ACTION_MOVE")
                    selectedImage?.let { imagen ->
                        if (event.pointerCount > 1) {
                            Log.d("OVERLAYVIEW", "escalar -> $selectedImage")
                            val newScale = imagen.image.let {
                                DrawFunctions.scaleImage(
                                    it.x,
                                    it.y,
                                    it.width,
                                    it.height,
                                    event,
                                    initialFingerSpacing,
                                    300f
                                )
                            }

                            Log.d("OVERLAYVIEW", "newScale: $newScale")
                            newScale?.let {
                                imagen.image.x = it.x
                                imagen.image.y = it.y
                                imagen.image.width = it.width
                                imagen.image.height = it.height

                                val layoutParams = imagen.view.ivMainImage.layoutParams
                                layoutParams.width = it.width.toInt()
                                layoutParams.height = it.height.toInt()
                                imagen.view.ivMainImage.layoutParams = layoutParams

                                // Si necesitas escalar la imagen dentro del ImageView
                                imagen.view.ivMainImage.scaleType = ImageView.ScaleType.CENTER_CROP
                            }
                        } else {

                            val deltaX = event.x - lastX
                            val deltaY = event.y - lastY

                            val newX = imagen.image.x + deltaX
                            val newY = imagen.image.y + deltaY
                            Log.d("OVERLAYVIEW", "mover $newX $newY  width $width  height $height")
                            moveImage(imagen, newX, newY)

                            lastX = event.x
                            lastY = event.y
                        }
                    }

                    selectedYoutube?.let { video ->
                        Log.d("OVERLAYVIEW", "video")
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
                            selectedYoutube?.let { video ->
                                Log.d("OVERLAYVIEW", "move video")

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
                    selectedYoutube = null
                }
            }

            return true
        }
        return super.onTouchEvent(event)
    }

    private fun findSelectedImage(x: Float, y: Float): ImageBitmapData? {
        val listImages = GlobalConfig.listMyWhiteBoard?.getAllImageBitmap()
        if (listImages?.isNotEmpty() == true) {
            listImages.forEach {
                Log.d("OVERLAYVIEW", "lista imagenes: ${it.image}")
                if (isPointInsideImageBitmapData(x, y, it)) {
                    return it
                }
            }
        }

        return null
    }

    private fun findSelectedVideo(x: Float, y: Float): YoutubeVideo? {
        for (video in youtubeVideos) {
            if (video.viewer?.let { isViewContains(it, x, y) } == true) {
                return video
            }
        }
        return null
    }

    private fun setNewPositionImage() {
        val list = GlobalConfig.listMyWhiteBoard

        if (list != null) {
            val imageBitmapAReemplazar: ImageBitmapData? = selectedImage?.image

            // Encuentra el índice del ImageBitmap2 que quieres reemplazar
            val index = list.lines.indexOfFirst { myLines ->
                myLines.listLines.any { myLine ->
                    myLine.imageBitmap == imageBitmapAReemplazar
                }
            }
            if (index != -1) {
                // Encuentra el índice de la MyLine dentro de la lista de MyLines
                val myLinesIndex = list.lines[index].listLines.indexOfFirst { myLine ->
                    myLine.imageBitmap == imageBitmapAReemplazar
                }

                if (myLinesIndex != -1) {
                    // Realiza el reemplazo del ImageBitmap2
                    list.lines[index].listLines.toMutableList().removeAt(myLinesIndex)

                    val newImage = selectedImage?.image?.apply {
                        this.rotation = rotateImage
                    }

                    Log.d("OVERLAYVIEW","new Image -> $newImage")

                    list.lines.add(
                        MyLines(
                            listOf(MyLine(null, null, null, newImage)),
                            GlobalConfig.backgroundWallpaper,
                            GlobalConfig.backgroundColor,
                            GlobalConfig.listYoutube.filter { it.page == GlobalConfig.currentPage },
                            GlobalConfig.currentPage
                        )
                    )

                    GlobalConfig.listMyWhiteBoard = list
                }
            }
        }

        //TODO: llamar whiteboard para render
        GlobalConfig.listMyWhiteBoard?.let {
            mainActivityContext?.findViewById<WriteBoard>(R.id.whiteboard)
                ?.drawSavedJson(it.lines.first { it.page == GlobalConfig.currentPage })
        }

        removeView(imageView)
        selectedImage = null
    }

    private fun deleteImage() {
        //TODO: quityar de la lista de imagenes a pintar
        val list = GlobalConfig.listMyWhiteBoard

        if (list != null) {
            val imageBitmapAReemplazar: ImageBitmapData? = selectedImage?.image

            // Encuentra el índice del ImageBitmap2 que quieres reemplazar
            val index = list.lines.indexOfFirst { myLines ->
                myLines.listLines.any { myLine ->
                    myLine.imageBitmap == imageBitmapAReemplazar
                }
            }
            if (index != -1) {
                // Encuentra el índice de la MyLine dentro de la lista de MyLines
                val myLinesIndex = list.lines[index].listLines.indexOfFirst { myLine ->
                    myLine.imageBitmap == imageBitmapAReemplazar
                }

                if (myLinesIndex != -1) {
                    val mutableList = list.lines[index].listLines.toMutableList()
                    mutableList.removeAt(myLinesIndex)
                    GlobalConfig.listMyWhiteBoard?.lines?.get(index)?.listLines = mutableList
                }
            }
        }

        //TODO: llamar whiteboard para render
        GlobalConfig.listMyWhiteBoard?.let {
            mainActivityContext?.findViewById<WriteBoard>(R.id.whiteboard)
                ?.drawSavedJson(it.lines.first { it.page == GlobalConfig.currentPage })
        }

        imageView?.let {
            removeView(it)
        }
        selectedImage = null
    }

    private fun isPointInsideImageBitmapData(
        pointX: Float,
        pointY: Float,
        imageBitmapData: ImageBitmapData
    ): Boolean {
        return pointX >= imageBitmapData.x &&
                pointX <= imageBitmapData.x + imageBitmapData.width &&
                pointY >= imageBitmapData.y &&
                pointY <= imageBitmapData.y + imageBitmapData.height
    }

    private fun resizeImage(originalBitmap: Bitmap): Bitmap {
        val maxDimension = 1200f // Máximo ancho y alto en píxeles

        val screenWidth = GlobalConfig.SCREEN_WIDTH
        val screenHeight = GlobalConfig.SCREEN_HEIGHT

        val bitmapWidth = originalBitmap.width.toFloat()
        val bitmapHeight = originalBitmap.height.toFloat()

        val ratio = bitmapHeight / bitmapWidth

        var newWidth: Float
        var newHeight: Float

        if (bitmapWidth > maxDimension || bitmapHeight > maxDimension) {
            if (bitmapWidth > bitmapHeight) {
                newWidth = maxDimension
                newHeight = maxDimension * ratio
            } else {
                newHeight = maxDimension
                newWidth = maxDimension / ratio
            }
        } else {
            newWidth = bitmapWidth
            newHeight = bitmapHeight
        }

        // Verifica si las dimensiones escaladas exceden las dimensiones de la pantalla
        if (newWidth > screenWidth || newHeight > screenHeight) {
            val ratioScreen = screenHeight / screenWidth

            if (newWidth > newHeight) {
                newWidth = screenWidth.toFloat()
                newHeight = screenWidth * ratio
            } else {
                newHeight = screenHeight.toFloat()
                newWidth = screenHeight / ratio
            }
        }

        return Bitmap.createScaledBitmap(
            originalBitmap,
            newWidth.toInt(),
            newHeight.toInt(),
            true
        )
    }

    private fun moveImage(imagenView: ImageDataView, newX: Float, newY: Float) {
        val adjustedX =
            newX.coerceIn(marginThreshold, width - imagenView.view.width - marginThreshold)
        val adjustedY =
            newY.coerceIn(marginThreshold, height - imagenView.view.height - marginThreshold)

        imagenView.image.x = adjustedX
        imagenView.image.y = adjustedY

        val layoutParams = imagenView.view.layoutParams as LayoutParams
        layoutParams.leftMargin = adjustedX.toInt()
        layoutParams.topMargin = adjustedY.toInt()
        imagenView.view.layoutParams = layoutParams
    }


    fun addListYouTubeVideos(videoList: List<YoutubeVideo>) {
        youtubeVideos.clear()
        if (videoList.size < GlobalConfig.numMaxYoutubePage) {
            for (video in videoList) {
                if (video.viewer == null) {
                    video.viewer = initializeYouTubePlayerView(context, video.id)
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
        selectedYoutube = null

        invalidate()
    }

    fun clearListYoutube() {
        youtubeVideos.clear()
        removeAllViews()
        selectedYoutube = null
        setDrawingMode()
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

    private fun initializeYouTubePlayerView(context: Context, videoId: String): YouTubePlayerView {
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

        playerView.initialize(listenner, options)

        return playerView
    }

}

data class ImageDataView(val view: ViewImage, var image: ImageBitmapData)

data class YoutubeVideo(
    val id: String,
    var viewer: YouTubePlayerView?,
    var x: Float,
    var y: Float,
    var width: Int,
    var height: Int,
    var rotation: Float,
    var scaleX: Float,
    var scaleY: Float,
    var page: Int
)