package com.orbys.demowhiteboard.data.api.model.youtube


import com.google.gson.annotations.SerializedName

data class Id(
  /*  @SerializedName("kind")
    val kind: String, // youtube#video*/
    @SerializedName("videoId")
    val videoId: String // CocEMWdc7Ck
)