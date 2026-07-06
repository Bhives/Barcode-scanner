package com.reproflex3.rplink.domain.model.request

import com.google.gson.annotations.SerializedName

data class RefreshTokenRequestBody(
    @SerializedName("token")
    val token: String,
    @SerializedName("refreshToken")
    val refreshToken: String
)