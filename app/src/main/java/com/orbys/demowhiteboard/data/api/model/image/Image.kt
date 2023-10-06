package com.orbys.demowhiteboard.data.api.model.image


import com.google.gson.annotations.SerializedName

data class Image(
    @SerializedName("byteSize")
    val byteSize: Int, // 282915
    @SerializedName("contextLink")
    val contextLink: String, // https://commons.wikimedia.org/wiki/File:Manzana.svg
    @SerializedName("height")
    val height: Int, // 1024
    @SerializedName("thumbnailHeight")
    val thumbnailHeight: Int, // 150
    @SerializedName("thumbnailLink")
    val thumbnailLink: String, // https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTZVi3WwFme-o7rcQH3qWihVxpPsu5uVEjxtuKK_M9Wf0RS4fpnrtl4IPs&s
    @SerializedName("thumbnailWidth")
    val thumbnailWidth: Int, // 150
    @SerializedName("width")
    val width: Int // 1024
)