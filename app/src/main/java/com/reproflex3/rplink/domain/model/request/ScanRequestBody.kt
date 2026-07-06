package com.reproflex3.rplink.domain.model.request

import com.google.gson.annotations.SerializedName

data class ScanRequestBody(
    @SerializedName("payload")
    val payload: String,
    @SerializedName("lat")
    val lat: Double = 0.0,
    @SerializedName("lon")
    val lon: Double = 0.0,
    @SerializedName("type")
    val type: String,
    @SerializedName("status")
    val status: String = "success"
)