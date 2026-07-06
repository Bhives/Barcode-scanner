package com.reproflex3.rplink.domain.model.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ApiResponse(
    @SerializedName("data")
    val data: ResponseData,
    @SerializedName("isSuccess")
    val isSuccess: Boolean,
) : Parcelable