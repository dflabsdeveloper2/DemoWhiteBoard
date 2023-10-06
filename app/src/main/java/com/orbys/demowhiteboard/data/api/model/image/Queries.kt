package com.orbys.demowhiteboard.data.api.model.image


import com.google.gson.annotations.SerializedName

data class Queries(
    @SerializedName("nextPage")
    val nextPage: List<NextPage>,
    @SerializedName("request")
    val request: List<Request>
)