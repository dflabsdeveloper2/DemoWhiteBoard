package com.orbys.demowhiteboard.data.api.model.youtube


import com.google.gson.annotations.SerializedName

data class Item(
   /* @SerializedName("etag")
    val etag: String, // 9LZPxhIO6sH7P3aocPf1WScdRmI*/
    @SerializedName("id")
    val id: Id,
  /*  @SerializedName("kind")
    val kind: String, // youtube#searchResult*/
    @SerializedName("snippet")
    val snippet: Snippet
)