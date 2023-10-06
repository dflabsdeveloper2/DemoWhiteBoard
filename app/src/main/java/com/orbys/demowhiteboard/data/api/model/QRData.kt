package com.orbys.demowhiteboard.data.api.model

data class QrDataJson (var encryption:Boolean, var password:Int?,var images: Array<ImageDataInfo>)

data class ResponseQR(var url:String)

data class ImageDataInfo(var file:String,var datetime:String)
