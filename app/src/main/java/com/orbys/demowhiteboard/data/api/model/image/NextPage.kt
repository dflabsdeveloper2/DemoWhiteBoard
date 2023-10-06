package com.orbys.demowhiteboard.data.api.model.image


import com.google.gson.annotations.SerializedName

data class NextPage(
    @SerializedName("count")
    val count: Int, // 10
    @SerializedName("cx")
    val cx: String, // e0b9072de156d4dbe
    @SerializedName("inputEncoding")
    val inputEncoding: String, // utf8
    @SerializedName("outputEncoding")
    val outputEncoding: String, // utf8
    @SerializedName("rights")
    val rights: String, // cc_publicdomain
    @SerializedName("safe")
    val safe: String, // active
    @SerializedName("searchTerms")
    val searchTerms: String, // manzana
    @SerializedName("searchType")
    val searchType: String, // image
    @SerializedName("startIndex")
    val startIndex: Int, // 11
    @SerializedName("title")
    val title: String, // Google Custom Search - manzana
    @SerializedName("totalResults")
    val totalResults: String // 725000
)