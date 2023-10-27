package com.orbys.demowhiteboard.domain.drawimage

import android.util.Log
import android.view.MotionEvent
import com.orbys.demowhiteboard.core.GlobalConfig
import com.orbys.demowhiteboard.domain.DrawFunctions
import com.orbys.demowhiteboard.domain.model.ImageBitmapData
import com.orbys.demowhiteboard.domain.model.MyLine
import com.orbys.demowhiteboard.domain.model.MyLines
import com.orbys.demowhiteboard.ui.interfaz.Distribute
import com.orbys.demowhiteboard.ui.whiteboard.WriteBoardController

class ImageBitmapDistribute(mController: WriteBoardController) : Distribute {
    private val mWriteBoardController: WriteBoardController
    private var selected: ImageBitmapData? = null
    private var initialFingerSpacing = 1f
    private var initialAngle: Float = 0f


    init {
        mWriteBoardController = mController
    }

    override fun onTouchEvent(event: MotionEvent) {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val x = event.x
                val y = event.y

                selected = hasBitmapAt(x, y)
                if (selected != null) {
                    initialFingerSpacing = DrawFunctions.getFingerSpacing(event)
                    initialAngle = selected!!.rotation
                    Log.d("IMAGE", "Image selected down-> ${selected?.x}  ${selected?.y}")
                } else {
                    //punto que he tocado

                }
            }

            MotionEvent.ACTION_MOVE -> {
                selected?.let { imageSelected->
                    Log.d("TRANSFORM", "count -> ${event.pointerCount}")
                    when (event.pointerCount) {
                        1 -> {
                            val temp = ImageBitmapData(
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

                                Log.d("TRANSFORM", "MOVE")
                                mWriteBoardController.moveBitmap(Pair(imageSelected, "move"))
                            } else {
                                Log.d("IMAGE", "Fuera de pantalla")
                                return // Evita que la imagen se actualice si está fuera de la pantalla
                            }
                        }

                        else -> {
                            Log.d("TRANSFORM", "SCALE FIRST")
                            // Calcula la nueva distancia entre los dedos para escalar
                            val newScale = DrawFunctions.scaleImage(
                                imageSelected.x,
                                imageSelected.y,
                                imageSelected.width,
                                imageSelected.height,
                                event,
                                initialFingerSpacing
                            )

                            newScale?.let {
                                imageSelected.x = it.x
                                imageSelected.y = it.y
                                imageSelected.width = it.width
                                imageSelected.height = it.height

                                Log.d("TRANSFORM", "SCALE")
                                //mWriteBoardController.moveBitmap(Pair(imageSelected, "scale"))
                            }

                            val currentAngle = DrawFunctions.calculateRotation(event)
                            Log.d("TRANSFORM", "angle-> $currentAngle")
                            // Calcula la diferencia de ángulo entre el ángulo actual y el inicial
                            val angleDelta = currentAngle - initialAngle
                            Log.d("TRANSFORM", "angle bo-> $angleDelta")

                            /*// Determina la dirección del giro (izquierda o derecha) y aplica la rotación
                            if (angleDelta > 0) {
                                // Gira hacia la derecha
                                // Aplica la rotación a tu vista o imagen en dirección a la derecha
                                // ... (aplica la rotación a tu vista o imagen aquí)

                                imageSelected.rotation += angleDelta

                            } else {
                                // Gira hacia la izquierda
                                // Aplica la rotación a tu vista o imagen en dirección a la izquierda
                                // ... (aplica la rotación a tu vista o imagen aquí)
                                imageSelected.rotation -= angleDelta
                            }*/

                            imageSelected.rotation += angleDelta

                            Log.d(
                                "TRANSFORM",
                                "rotation-> ${DrawFunctions.normalizeAngle(imageSelected.rotation.toInt())}"
                            )
                            imageSelected.rotation =
                                DrawFunctions.normalizeAngle(imageSelected.rotation.toInt())
                                    .toFloat()
                            mWriteBoardController.moveBitmap(Pair(imageSelected, "rotate"))

                            initialAngle = currentAngle
                        }
                    }
                }

                //linea intermitente
            }

            MotionEvent.ACTION_UP -> {
                initialFingerSpacing = 1f
                if (selected != null) {
                    mWriteBoardController.moveBitmap(Pair(selected!!, "finish"))

                    val list = GlobalConfig.listMyWhiteBoard

                    if (list != null) {
                        val imageBitmapAReemplazar: ImageBitmapData = selected!!

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
                                /*list.lines[index].listLines[myLinesIndex].imageBitmap =
                                    imageBitmapAReemplazar*/

                                list.lines[index].listLines.toMutableList().removeAt(myLinesIndex)

                                list.lines.add(
                                    MyLines(
                                        listOf(MyLine(null, null, null, selected)),
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

                    GlobalConfig.listMyWhiteBoard?.let {
                        mWriteBoardController.drawSaved(it.lines.first { it.page == GlobalConfig.currentPage })
                    }
                }

                selected = null

                //al levantar calcular si se ha cerrado, y las lineas dentro
            }
        }
    }

    private fun hasBitmapAt(x: Float, y: Float): ImageBitmapData? {

        val listBitmap = GlobalConfig.listMyWhiteBoard?.getAllImageBitmap()
        Log.d("IMAGE", "listBitmap size: ${listBitmap?.size}")
        if (listBitmap.isNullOrEmpty()) return null

        for (imageBitmap in listBitmap) {
            Log.d(
                "IMAGE",
                "Image selected ${imageBitmap.image}-> ${imageBitmap.x}  ${imageBitmap.y}"
            )
            /*   val matrix = Matrix()
               // Aplicar la rotación inversa a las coordenadas del punto
               matrix.postRotate(
                   -imageBitmap.rotation,
                   imageBitmap.width / 2f,
                   imageBitmap.height / 2f
               )
               val inverseMatrix = Matrix()
               matrix.invert(inverseMatrix)
               val transformedPoint = floatArrayOf(x, y)
               inverseMatrix.mapPoints(transformedPoint)*/
            val transformedPoint = floatArrayOf(x, y)
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
        imageBitmap: ImageBitmapData,
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