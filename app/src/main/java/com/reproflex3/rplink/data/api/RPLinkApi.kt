package com.reproflex3.rplink.data.api

import com.reproflex3.rplink.domain.model.request.LoginRequestBody
import com.reproflex3.rplink.domain.model.request.RefreshTokenRequestBody
import com.reproflex3.rplink.domain.model.request.ScanRequestBody
import com.reproflex3.rplink.domain.model.response.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface RPLinkApi {

    @POST("auth/login")
    suspend fun login(
        @Body requestBody: LoginRequestBody
    ): Response<ApiResponse>

    @GET("auth/current-user")
    suspend fun getCurrentUser(
        @Header("Authorization") token: String
    ): Response<ApiResponse>

    @POST("auth/refresh-token")
    suspend fun refreshToken(
        @Body requestBody: RefreshTokenRequestBody
    ): Response<ApiResponse>

    @POST("scans/add")
    suspend fun scanAdd(
        @Header("Authorization") token: String,
        @Body requestBody: ScanRequestBody
    ): Response<ApiResponse>

    @POST("auth/logout")
    suspend fun logout(
        @Header("Authorization") token: String,
        @Body body: Any = Object()
    ): Response<ApiResponse>
}