package com.orbys.demowhiteboard.data.api.model.youtube


import com.google.gson.annotations.SerializedName

data class Snippet(
   /* @SerializedName("channelId")
    val channelId: String, // UCmS75G-98QihSusY7NfCZtw
    @SerializedName("channelTitle")
    val channelTitle: String, // Bizarrap
    @SerializedName("description")
    val description: String, // SHAKIRA || BZRP Music Sessions #53 Lyrics by: https://www.instagram.com/shakira Beat by: https://www.instagram.com/bizarrap ...
    @SerializedName("liveBroadcastContent")
    val liveBroadcastContent: String, // none
    @SerializedName("publishTime")
    val publishTime: String, // 2023-01-12T00:00:07Z
    @SerializedName("publishedAt")
    val publishedAt: String, // 2023-01-12T00:00:07Z*/
    @SerializedName("thumbnails")
    val thumbnails: Thumbnails,
    @SerializedName("title")
    val title: String // SHAKIRA || BZRP Music Sessions #53
)