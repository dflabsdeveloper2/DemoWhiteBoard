package com.orbys.demowhiteboard.ui.view

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.orbys.demowhiteboard.R
import com.orbys.demowhiteboard.core.GlobalConfig
import com.orbys.demowhiteboard.domain.DrawFunctions
import com.orbys.demowhiteboard.domain.model.ImageBitmapData
import com.orbys.demowhiteboard.domain.model.MyLine
import com.orbys.demowhiteboard.domain.model.MyLines
import com.orbys.demowhiteboard.ui.MainActivity
import com.orbys.demowhiteboard.ui.whiteboard.WriteBoard

class OverlayView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    private var isMoveModeEnabled = false
    private val marginThreshold = 10
    private var selectedImage: ImageDataView? = null
    var imageView: ViewImage? = null
    private var lastX = 0f
    private var lastY = 0f
    private var rotateImage = 0f
    private val mainActivityContext: MainActivity? = context as? MainActivity

    fun setMovementMode() {
        isMoveModeEnabled = true
    }

    fun setDrawingMode() {
        isMoveModeEnabled = false
        imageView?.let {
            removeView(it)
        }
        selectedImage = null
    }

    private fun addImageFloating(imageBitmap: ImageBitmapData) {

        if (selectedImage != null) return

        val layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        layoutParams.gravity = Gravity.CENTER // Modifica según tus necesidades

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
                        this.x = lastX
                        this.y = lastY
                        this.rotation = rotateImage
                    }

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

    /* fun addImageFloatingFirst(imageBitmap: ImageBitmap) {
         isFirst = true

         val layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
         layoutParams.gravity = Gravity.CENTER // Modifica según tus necesidades

         val imageView = ViewImage(context)
         // Agregar imageView a la vista OverlayView
         addView(imageView, layoutParams)

         selectedImage = ImageDataView(
             imageView,
             ImageBitmapData(
                 imageBitmap.image,
                 (width / 2).toFloat(),
                 (height / 2).toFloat(),
                 imageBitmap.image.width.toFloat(),
                 imageBitmap.image.height.toFloat()
             )
         )

         // Configurar la imagen en el imageView
         val mainImage = imageView.findViewById<ImageView>(R.id.ivMainImage)
         mainImage.setImageBitmap(resizeImage(imageBitmap.image))

         val btnClose = imageView.findViewById<ImageView>(R.id.ivCloseMainImage)
         val btnOk = imageView.findViewById<ImageView>(R.id.ivOkMainImage)

         val btnRotation = imageView.findViewById<ImageView>(R.id.ivItemRotationMainImagen)

         var lastRotationX = 0f
         var lastRotationY = 0f

         btnRotation.setOnTouchListener { view, motionEvent ->
             if (isMoveModeEnabled) { // Asegurarse de que el modo de movimiento está habilitado
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

                         imageView.rotation += rotationAngle

                         lastRotationX = motionEvent.x
                         lastRotationY = motionEvent.y
                     }
                 }
                 return@setOnTouchListener true
             }
             return@setOnTouchListener false // Si no está en modo de movimiento, no manejar el evento
         }

         btnClose.setOnClickListener {
             if (isMoveModeEnabled) {
                 //TODO: quityar de la lista de imagenes a pintar
                 removeView(imageView)
                 selectedImage = null
             }
         }

         btnOk.setOnClickListener {
             if (isMoveModeEnabled) {
                 //TODO: guardar estado en el que esta (x e y, rotacion) para pintarlo
                 removeView(imageView)
                 selectedImage = null
             }
         }
     }*/

    //TODO:REVISAR si es necesario
    /* override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
         return isMoveModeEnabled
     }*/

    private var initialFingerSpacing = 1f
    private var initialScaleX = 1f
    private var initialScaleY = 1f

    override fun onTouchEvent(event: MotionEvent): Boolean {
        Log.d("OVERLAYVIEW", "touch")
        if (isMoveModeEnabled) {
            Log.d("OVERLAYVIEW", "touch is movemode")
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    lastX = event.x
                    lastY = event.y
                    val selectImageBitmap = findSelectedImage(event.x, event.y)
                    initialFingerSpacing = DrawFunctions.getFingerSpacing(event)

                    if (selectImageBitmap != null) {
                        if (selectedImage == null) {
                            Log.d("OVERLAYVIEW", "segunda")
                            addImageFloating(selectImageBitmap)
                        }
                    } else {
                        if (selectedImage == null) {
                            Log.d("OVERLAYVIEW", "no encontrada imagen")
                            return super.onTouchEvent(event)
                        }
                    }
                }

                MotionEvent.ACTION_MOVE -> {
                    Log.d("OVERLAYVIEW", "ACTION_MOVE")
                    selectedImage?.let { imagen ->
                        if (event.pointerCount > 1) {
                            Log.d("OVERLAYVIEW", "escalar")
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
                            newScale?.let {
                                imagen.image.x = it.x
                                imagen.image.y = it.y
                                imagen.image.width = it.width
                                imagen.image.height = it.height

                                val layoutParams =
                                    imagen.view.layoutParams as ConstraintLayout.LayoutParams
                                layoutParams.width = imagen.view.width
                                layoutParams.height = imagen.view.height
                                layoutParams.leftMargin = imagen.view.x.toInt()
                                layoutParams.topMargin = imagen.view.y.toInt()
                                imagen.view.layoutParams = layoutParams
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
                }

                MotionEvent.ACTION_UP -> {
                    initialFingerSpacing = 1f
                    initialScaleX = 1f
                    initialScaleY = 1f
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
        val maxWidthDp = 300 // Ancho máximo en dp
        val maxHeightDp = 300 // Altura máxima en dp

        val density = resources.displayMetrics.density
        val maxWidthPx = (maxWidthDp * density + 0.5f).toInt() // Ancho máximo en píxeles
        val maxHeightPx = (maxHeightDp * density + 0.5f).toInt() // Altura máxima en píxeles

        val bitmapWidth = originalBitmap.width
        val bitmapHeight = originalBitmap.height

        val ratio = bitmapHeight.toFloat() / bitmapWidth.toFloat()

        val newWidth = if (bitmapWidth > maxWidthPx || bitmapHeight > maxHeightPx) {
            if (bitmapWidth > bitmapHeight) {
                maxWidthPx
            } else {
                (maxHeightPx / ratio).toInt()
            }
        } else {
            bitmapWidth
        }

        val newHeight = if (bitmapHeight > maxHeightPx || bitmapWidth > maxWidthPx) {
            if (bitmapHeight > bitmapWidth) {
                maxHeightPx
            } else {
                (maxWidthPx * ratio).toInt()
            }
        } else {
            bitmapHeight
        }

        return Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
    }

    private fun moveImage(imagenView: ImageDataView, newX: Float, newY: Float) {
        val maxX = width / 2 - imagenView.image.width - marginThreshold
        val maxY =
            height / 2 - 300 - imagenView.image.height - marginThreshold //TODO: revisar si el height es toda la pantalla
        val limitedX = newX.coerceIn(-width / 2 + marginThreshold.toFloat(), maxX)
        val limitedY = newY.coerceIn(-height / 2 + marginThreshold.toFloat(), maxY)

        imagenView.image.x = limitedX
        imagenView.image.y = limitedY

        val layoutParams = imagenView.view.layoutParams as LayoutParams
        layoutParams.leftMargin = limitedX.toInt()
        layoutParams.topMargin = limitedY.toInt()
        imagenView.view.layoutParams = layoutParams
    }
}

data class ImageDataView(val view: ViewImage, var image: ImageBitmapData)