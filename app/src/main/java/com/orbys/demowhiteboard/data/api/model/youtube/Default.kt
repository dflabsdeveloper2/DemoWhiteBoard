package com.orbys.demowhiteboard.data.api.model.youtube


import com.google.gson.annotations.SerializedName

data class Default(
    @SerializedName("height")
    val height: Int, // 90
    @SerializedName("url")
    val url: String, // https://i.ytimg.com/vi/CocEMWdc7Ck/default.jpg
    @SerializedName("width")
    val width: Int // 120
)