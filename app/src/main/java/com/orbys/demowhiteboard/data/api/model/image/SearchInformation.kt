package com.orbys.demowhiteboard.data.api.model.image


import com.google.gson.annotations.SerializedName

data class SearchInformation(
    @SerializedName("formattedSearchTime")
    val formattedSearchTime: String, // 0.69
    @SerializedName("formattedTotalResults")
    val formattedTotalResults: String, // 725,000
    @SerializedName("searchTime")
    val searchTime: Double, // 0.685713
    @SerializedName("totalResults")
    val totalResults: String // 725000
)