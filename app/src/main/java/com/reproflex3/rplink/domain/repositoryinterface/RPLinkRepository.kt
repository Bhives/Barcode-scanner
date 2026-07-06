package com.reproflex3.rplink.domain.repositoryinterface

import com.reproflex3.rplink.presentation.model.ApiOutputResponse
import com.reproflex3.rplink.presentation.model.LogoutResponse
import kotlinx.coroutines.flow.Flow

interface RPLinkRepository {

    suspend fun login(email: String, password: String): ApiOutputResponse

    suspend fun getCurrentUser(): ApiOutputResponse

    suspend fun getScanResult(
        payload: String,
        lat: Double,
        lon: Double,
        type: String
    ): ApiOutputResponse

    suspend fun logout(): ApiOutputResponse

    suspend fun observeLogoutState(): Flow<LogoutResponse>
}