package com.orbys.demowhiteboard.data.api.model.youtube


import com.google.gson.annotations.SerializedName

data class PageInfo(
    @SerializedName("resultsPerPage")
    val resultsPerPage: Int, // 50
    @SerializedName("totalResults")
    val totalResults: Int // 1000000
)