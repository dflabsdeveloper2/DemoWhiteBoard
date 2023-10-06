package com.orbys.demowhiteboard.data.api.model.youtube


import com.google.gson.annotations.SerializedName

data class ListVideoModelApi(
   /* @SerializedName("etag")
    val etag: String, // -BvR2ThV1Ih5ZU0L5TQhicMaxgg*/
    @SerializedName("items")
    val items: List<Item>,
   /* @SerializedName("kind")
    val kind: String, // youtube#searchListResponse
    @SerializedName("nextPageToken")
    val nextPageToken: String, // CDIQAA
    @SerializedName("pageInfo")
    val pageInfo: PageInfo,
    @SerializedName("regionCode")
    val regionCode: String // ES*/
)