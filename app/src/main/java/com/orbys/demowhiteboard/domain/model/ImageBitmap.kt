package com.orbys.demowhiteboard.domain.model

import android.graphics.Bitmap

data class ImageBitmap(var image:Bitmap, var page:Int)

data class ImageBitmap2(var image:Bitmap, var x :Float,var y:Float, var width:Float,var height:Float, var rotation:Float=0f)