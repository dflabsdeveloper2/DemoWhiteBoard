package com.orbys.demowhiteboard.data.api.model.image


import com.google.gson.annotations.SerializedName

data class ImageModelApi(
   /* @SerializedName("displayLink")
    val displayLink: String, // commons.wikimedia.org
    @SerializedName("fileFormat")
    val fileFormat: String, // image/png
    @SerializedName("htmlSnippet")
    val htmlSnippet: String, // File:<b>Manzana</b>.svg - Wikimedia Commons
    @SerializedName("htmlTitle")
    val htmlTitle: String, // File:<b>Manzana</b>.svg - Wikimedia Commons
    @SerializedName("image")
    val image: Image,
    @SerializedName("kind")
    val kind: String, // customsearch#result*/
    @SerializedName("link")
    val link: String, // https://upload.wikimedia.org/wikipedia/commons/thumb/0/02/Manzana.svg/1024px-Manzana.svg.png
   /* @SerializedName("mime")
    val mime: String, // image/png
    @SerializedName("snippet")
    val snippet: String, // File:Manzana.svg - Wikimedia Commons*/
    @SerializedName("title")
    val title: String // File:Manzana.svg - Wikimedia Commons
)