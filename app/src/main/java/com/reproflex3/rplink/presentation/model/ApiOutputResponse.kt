package com.reproflex3.rplink.presentation.model

import com.reproflex3.rplink.domain.model.response.ResponseData

sealed class ApiOutputResponse {

    data class Success(val data: ResponseData?) : ApiOutputResponse()

    data class Failure(val exception: Exception) : ApiOutputResponse()
}