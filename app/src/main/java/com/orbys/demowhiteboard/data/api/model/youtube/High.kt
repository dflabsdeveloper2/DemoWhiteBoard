package com.orbys.demowhiteboard.data.api.model.youtube


import com.google.gson.annotations.SerializedName

data class High(
    @SerializedName("height")
    val height: Int, // 360
    @SerializedName("url")
    val url: String, // https://i.ytimg.com/vi/CocEMWdc7Ck/hqdefault.jpg
    @SerializedName("width")
    val width: Int // 480
)