package com.orbys.demowhiteboard.data.api.model.youtube


import com.google.gson.annotations.SerializedName

data class Medium(
  /*  @SerializedName("height")
    val height: Int, // 180*/
    @SerializedName("url")
    val url: String, // https://i.ytimg.com/vi/CocEMWdc7Ck/mqdefault.jpg
  /*  @SerializedName("width")
    val width: Int // 320*/
)