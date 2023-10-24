package com.orbys.demowhiteboard.domain.drawimage

import android.graphics.Matrix
import android.util.Log
import android.view.MotionEvent
import com.orbys.demowhiteboard.core.GlobalConfig
import com.orbys.demowhiteboard.domain.model.ImageBitmap2
import com.orbys.demowhiteboard.ui.core.DrawFunctions
import com.orbys.demowhiteboard.ui.interfaz.Distribute
import com.orbys.demowhiteboard.ui.whiteboard.WriteBoardController

class ImageBitmapDistribute(mController: WriteBoardController) : Distribute {
    private val mWriteBoardController: WriteBoardController
    private var selected: ImageBitmap2? = null
    private var initialFingerSpacing = 1f

    init {
        mWriteBoardController = mController
    }

    override fun onTouchEvent(event: MotionEvent) {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val x = event.x
                val y = event.y

                selected = hasBitmapAt(x, y)
                initialFingerSpacing = DrawFunctions.getFingerSpacing(event)

                Log.d("IMAGE", "Image selected down-> ${selected?.x}  ${selected?.y}")
            }

            MotionEvent.ACTION_MOVE -> {
                selected?.let { imageSelected->
                    if (event.pointerCount > 1) {
                        // Calcula la nueva distancia entre los dedos para escalar
                        val newScale = DrawFunctions.scaleImage(imageSelected.x,imageSelected.y,imageSelected.width,imageSelected.height,event,initialFingerSpacing)

                        newScale?.let {
                            imageSelected.x = it.x
                            imageSelected.y = it.y
                            imageSelected.width = it.width
                            imageSelected.height = it.height
                            mWriteBoardController.moveBitmap(Pair(imageSelected, "scale"))
                        }
                    } else {
                        val temp = ImageBitmap2(
                            imageSelected.image,
                            (event.x - imageSelected.width / 2),  // Calcula la nueva coordenada X del centro del bitmap
                            (event.y - imageSelected.height / 2), // Calcula la nueva coordenada Y del centro del bitmap
                            imageSelected.width,
                            imageSelected.height,
                            imageSelected.rotation
                        )

                        // Comprueba si las nuevas coordenadas están dentro de los límites de la pantalla
                        if (isImageBitmapWithinScreen(
                                temp,
                                GlobalConfig.SCREEN_WIDTH,
                                GlobalConfig.SCREEN_HEIGHT
                            )
                        ) {
                            imageSelected.x = temp.x
                            imageSelected.y = temp.y

                            mWriteBoardController.moveBitmap(Pair(imageSelected, "move"))
                        } else {
                            Log.d("IMAGE", "Fuera de pantalla")
                            return // Evita que la imagen se actualice si está fuera de la pantalla
                        }
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                initialFingerSpacing = 1f
                if (selected != null) {
                    val list = GlobalConfig.listMyWhiteBoard

                    if (list != null) {
                        val imageBitmapAReemplazar: ImageBitmap2 = selected!!
                        Log.d(
                            "IMAGE",
                            "Image selected a remplazar-> ${selected?.x}  ${selected?.y}"
                        )

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
                                list.lines[index].listLines[myLinesIndex].imageBitmap =
                                    imageBitmapAReemplazar

                                GlobalConfig.listMyWhiteBoard = list
                            }
                        }
                    }

                    selected = null

                    GlobalConfig.listMyWhiteBoard?.let {
                        mWriteBoardController.drawSaved(it.lines.first { it.page == GlobalConfig.currentPage })
                    }
                }
            }
        }
    }

    private fun hasBitmapAt(x: Float, y: Float): ImageBitmap2? {

        val listBitmap = GlobalConfig.listMyWhiteBoard?.getAllImageBitmap()
        Log.d("IMAGE", "listBitmap size: ${listBitmap?.size}")
        if (listBitmap.isNullOrEmpty()) return null

        for (imageBitmap in listBitmap) {
            Log.d("IMAGE", "Image selected ${imageBitmap.image}-> ${selected?.x}  ${selected?.y}")
            val matrix = Matrix()
            // Aplicar la rotación inversa a las coordenadas del punto
            matrix.postRotate(
                -imageBitmap.rotation,
                imageBitmap.width / 2f,
                imageBitmap.height / 2f
            )
            val inverseMatrix = Matrix()
            matrix.invert(inverseMatrix)
            val transformedPoint = floatArrayOf(x, y)
            inverseMatrix.mapPoints(transformedPoint)

            val left = imageBitmap.x
            val top = imageBitmap.y
            val right = left + imageBitmap.width
            val bottom = top + imageBitmap.height

            if (transformedPoint[0] in left..right &&
                transformedPoint[1] >= top && transformedPoint[1] <= bottom
            ) {
                // El punto (x, y) está dentro de las dimensiones del bitmap después de aplicar la rotación
                return imageBitmap
            }
        }
        // El punto (x, y) no está tocando ningún bitmap en la lista después de aplicar la rotación
        return null
    }

    private fun isImageBitmapWithinScreen(
        imageBitmap: ImageBitmap2,
        screenWidth: Int,
        screenHeight: Int
    ): Boolean {
        val left = imageBitmap.x
        val top = imageBitmap.y
        val right = left + imageBitmap.width
        val bottom = top + imageBitmap.height

        // Comprobar que los límites del objeto no se salgan de la pantalla
        return left >= 0 && top >= 0 && right <= screenWidth && bottom <= screenHeight
    }
}