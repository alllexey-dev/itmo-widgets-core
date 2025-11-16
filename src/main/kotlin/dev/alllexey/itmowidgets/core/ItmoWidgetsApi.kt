package dev.alllexey.itmowidgets.core

import dev.alllexey.itmowidgets.core.model.*
import retrofit2.http.*

interface ItmoWidgetsApi {

    // region auth & sessions & devices

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

    // endregion auth & sessions & devices

    // region app

    @GET("/api/app/version")
    suspend fun latestAppVersion(): ApiResponse<String>

    // endregion app

    // region sport

    @GET("/api/sport/filter/all")
    suspend fun allSportFilters(): ApiResponse<List<SportFilterResponse>>

    @DELETE("/api/sport/filter/all")
    suspend fun deleteAllSportFilters(): ApiResponse<String>

    @POST("/api/sport/filter/create")
    suspend fun createSportFilter(@Body request: SportFilterRequest): ApiResponse<SportFilterResponse>

    @POST("/api/sport/filter/{id}")
    suspend fun editSportFilter(@Path("id") id: Long, @Body request: SportFilterRequest): ApiResponse<SportFilterResponse>

    @DELETE("/api/sport/filter/{id}")
    suspend fun deleteSportFilter(@Path("id") id: Long): ApiResponse<String>

    @GET("/api/sport/free-sign/all")
    suspend fun allFreeSignEntries(): ApiResponse<List<SportFreeSignEntry>>

    @POST("/api/sport/free-sign/create")
    suspend fun createFreeSignEntry(@Body request: SportFreeSignRequest): ApiResponse<SportFreeSignEntry>

    @DELETE("/api/sport/free-sign/{id}")
    suspend fun deleteFreeSignEntry(@Path("id") id: Long): ApiResponse<String>

    // endregion sport
}