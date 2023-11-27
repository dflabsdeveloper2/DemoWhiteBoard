package com.orbys.demowhiteboard.data.api.model.image


import com.google.gson.annotations.SerializedName

data class ListImageModelApi(
    /*   @SerializedName("context")
       val context: Context,*/
    @SerializedName("items")
    val items: List<ImageModelApi>?,
    /*@SerializedName("kind")
    val kind: String, // customsearch#search
    @SerializedName("queries")
    val queries: Queries,
    @SerializedName("searchInformation")
    val searchInformation: SearchInformation,
    @SerializedName("url")
    val url: Url*/
)