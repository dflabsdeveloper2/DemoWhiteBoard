package com.orbys.demowhiteboard.domain.model

import android.graphics.Bitmap
import android.graphics.RectF

data class ImageBitmap(var image:Bitmap, var page:Int)

data class ImageBitmapData(var image:Bitmap, var x :Float, var y:Float, var width:Float, var height:Float, var rotation:Float=0f)

data class ImageTransformResult(val bitmap: Bitmap, val rectF: RectF)

data class ImageTransformResultData(val bitmapData: ImageBitmapData, val rectF: RectF)
data class ScaleResult(val x:Float,val y:Float,val width: Float,val height: Float)