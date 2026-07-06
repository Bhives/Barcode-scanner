package com.reproflex3.rplink.domain.model.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ResponseData(
    @SerializedName("token")
    val token: String?,
    @SerializedName("refreshToken")
    val refreshToken: String?,
    @SerializedName("expiresOn")
    val expiresOn: String?,
    @SerializedName("url")
    val url: String?,
    @SerializedName("publicId")
    val publicId: String?,
    @SerializedName("email")
    val email: String?
) : Parcelable