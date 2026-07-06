package com.reproflex3.rplink.data.repository

import com.reproflex3.rplink.data.api.RPLinkApi
import com.reproflex3.rplink.data.preferences.PreferencesManager
import com.reproflex3.rplink.data.preferences.PreferencesManagerImpl.Companion.REFRESH_TOKEN
import com.reproflex3.rplink.data.preferences.PreferencesManagerImpl.Companion.TOKEN
import com.reproflex3.rplink.domain.model.request.LoginRequestBody
import com.reproflex3.rplink.domain.model.request.RefreshTokenRequestBody
import com.reproflex3.rplink.domain.model.request.ScanRequestBody
import com.reproflex3.rplink.domain.repositoryinterface.RPLinkRepository
import com.reproflex3.rplink.presentation.model.ApiOutputResponse
import com.reproflex3.rplink.presentation.model.LogoutResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException
import java.net.UnknownHostException
import javax.inject.Inject

class RPLinkRepositoryImpl @Inject constructor(
    private val rpLinkApi: RPLinkApi,
    private val preferencesManager: PreferencesManager
) : RPLinkRepository {

    private val shouldLogout = MutableStateFlow(LogoutResponse())

    override suspend fun observeLogoutState(): Flow<LogoutResponse> {
        return shouldLogout
    }

    override suspend fun login(email: String, password: String): ApiOutputResponse {
        return sendRequest {
            val response = rpLinkApi.login(LoginRequestBody(email, password))
            if (response.isSuccessful) {
                shouldLogout.emit(LogoutResponse(false))
                ApiOutputResponse.Success(response.body()?.data)
            } else {
                ApiOutputResponse.Failure(Exception(convertErrorBody(response.errorBody())))
            }
        }
    }

    override suspend fun getCurrentUser(): ApiOutputResponse {
        return sendRequest {
            val token = "$BEARER  ${preferencesManager.getString(TOKEN)}"
            val currentUserResponse = rpLinkApi.getCurrentUser(token)
            if (currentUserResponse.code() != ERROR_401) {
                if (currentUserResponse.isSuccessful) {
                    shouldLogout.emit(LogoutResponse(false))
                    ApiOutputResponse.Success(currentUserResponse.body()?.data)
                } else {
                    ApiOutputResponse.Failure(Exception(convertErrorBody(currentUserResponse.errorBody())))
                }
            } else {
                when (val refreshTokenResponse = refreshToken()) {
                    is ApiOutputResponse.Success -> {
                        getCurrentUser()
                    }
                    is ApiOutputResponse.Failure -> {
                        ApiOutputResponse.Failure(refreshTokenResponse.exception)
                    }
                }
            }
        }
    }

    override suspend fun getScanResult(
        payload: String,
        lat: Double,
        lon: Double,
        type: String
    ): ApiOutputResponse {
        return sendRequest {
            val token = "$BEARER  ${preferencesManager.getString(TOKEN)}"
            val getScanResponse = rpLinkApi.scanAdd(token, ScanRequestBody(payload, lat, lon, type))
            if (getScanResponse.code() != ERROR_401) {
                if (getScanResponse.isSuccessful) {
                    ApiOutputResponse.Success(getScanResponse.body()?.data)
                } else {
                    ApiOutputResponse.Failure(Exception(convertErrorBody(getScanResponse.errorBody())))
                }
            } else {
                when (val refreshTokenResponse = refreshToken()) {
                    is ApiOutputResponse.Success -> {
                        getScanResult(payload, lat, lon, type)
                    }
                    is ApiOutputResponse.Failure -> {
                        ApiOutputResponse.Failure(refreshTokenResponse.exception)
                    }
                }
            }
        }
    }

    override suspend fun logout(): ApiOutputResponse {
        return sendRequest {
            val token = "$BEARER  ${preferencesManager.getString(TOKEN)}"
            val response = rpLinkApi.logout(token)
            if (response.code() != ERROR_401) {
                if (response.isSuccessful) {
                    shouldLogout.emit(LogoutResponse(true))
                    ApiOutputResponse.Success(response.body()?.data)
                } else {
                    ApiOutputResponse.Failure(Exception(convertErrorBody(response.errorBody())))
                }
            } else {
                val exception = Exception(convertErrorBody(response.errorBody()))
                shouldLogout.emit(LogoutResponse(true, exception.message.toString()))
                ApiOutputResponse.Failure(exception)
            }
        }
    }

    private suspend fun refreshToken(): ApiOutputResponse {
        val token = preferencesManager.getString(TOKEN)
        val refreshToken = preferencesManager.getString(REFRESH_TOKEN)
        val response =
            rpLinkApi.refreshToken(RefreshTokenRequestBody(token, refreshToken))
        if (response.code() != ERROR_401) {
            return if (response.isSuccessful) {
                with(response.body()?.data) {
                    preferencesManager.putString(
                        TOKEN,
                        this?.token.toString()
                    )
                    preferencesManager.putString(
                        REFRESH_TOKEN,
                        this?.refreshToken.toString()
                    )
                }
                ApiOutputResponse.Success(response.body()?.data)
            } else {
                ApiOutputResponse.Failure(Exception(convertErrorBody(response.errorBody())))
            }
        } else {
            val exception = Exception(convertErrorBody(response.errorBody()))
            shouldLogout.emit(LogoutResponse(true, exception.message.toString()))
            return ApiOutputResponse.Failure(exception)
        }
    }

    private suspend fun sendRequest(request: suspend () -> ApiOutputResponse): ApiOutputResponse {
        return try {
            request.invoke()
        } catch (unknownHostException: UnknownHostException) {
            ApiOutputResponse.Failure(unknownHostException)
        } catch (httpException: HttpException) {
            ApiOutputResponse.Failure(httpException)
        } catch (jsonException: JSONException) {
            ApiOutputResponse.Failure(jsonException)
        }
    }

    private fun convertErrorBody(errorBody: ResponseBody?): String {
        return JSONObject(errorBody?.string().toString()).getJSONObject(FAILURE)
            .getString(MESSAGE)
    }

    companion object {
        private const val FAILURE = "failure"
        private const val MESSAGE = "message"
        private const val BEARER = "Bearer"
        private const val ERROR_401 = 401
    }
}