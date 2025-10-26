package dev.alllexey.itmowidgets.core

import dev.alllexey.itmowidgets.core.model.ApiResponse
import dev.alllexey.itmowidgets.core.model.ItmoTokenLoginRequest
import dev.alllexey.itmowidgets.core.model.LogoutRequest
import dev.alllexey.itmowidgets.core.model.RefreshTokenRequest
import dev.alllexey.itmowidgets.core.model.RegisterDeviceRequest
import dev.alllexey.itmowidgets.core.model.SessionResponse
import dev.alllexey.itmowidgets.core.model.TokenResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST

interface ItmoWidgetsApi {

    @POST("/api/auth/itmo-token")
    suspend fun loginViaItmoToken(@Body request: ItmoTokenLoginRequest): ApiResponse<TokenResponse>

    @POST("/api/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): ApiResponse<TokenResponse>

    @POST("/api/auth/logout")
    suspend fun logout(@Body request: LogoutRequest): ApiResponse<String>

    @POST("/api/device/register-device")
    suspend fun registerToken(@Body request: RegisterDeviceRequest): ApiResponse<String>

    @GET("/api/session/all")
    suspend fun allSessions(): ApiResponse<List<SessionResponse>>

    @DELETE("/api/session/all")
    suspend fun endAllSessions(): ApiResponse<String>

    @GET("/api/app/version")
    suspend fun getLatestAppVersion(): ApiResponse<String>
}