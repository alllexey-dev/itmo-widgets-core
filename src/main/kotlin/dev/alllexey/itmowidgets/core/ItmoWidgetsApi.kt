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

    // region free sign

    @GET("/api/sport/free-sign/entry/my")
    suspend fun mySportFreeSignEntries(@Query("limit") limit: Long?): ApiResponse<List<SportFreeSignEntry>>

    @POST("/api/sport/free-sign/entry/create")
    suspend fun createSportFreeSignEntry(@Body request: SportFreeSignRequest): ApiResponse<SportFreeSignEntry>

    @DELETE("/api/sport/free-sign/entry/{id}")
    suspend fun deleteSportFreeSignEntry(@Path("id") id: Long): ApiResponse<String>

    @GET("/api/sport/free-sign/queue/current")
    suspend fun currentSportFreeSignQueues(): ApiResponse<List<SportFreeSignQueue>>

    @GET("/api/sport/free-sign/entry/{id}/mark-satisfied")
    suspend fun markSportFreeSignEntrySatisfied(@Path("id") id: Long): ApiResponse<String>
    // endregion free sign

    // region auto sign

    @GET("/api/sport/auto-sign/limits")
    suspend fun sportAutoSignLimits(): ApiResponse<SportAutoSignLimits>

    @GET("/api/sport/auto-sign/entry/my")
    suspend fun mySportAutoSignEntries(@Query("limit") limit: Long?): ApiResponse<List<SportAutoSignEntry>>

    @POST("/api/sport/auto-sign/entry/create")
    suspend fun createSportAutoSignEntry(@Body request: SportAutoSignRequest): ApiResponse<SportAutoSignEntry>

    @DELETE("/api/sport/auto-sign/entry/{id}")
    suspend fun deleteSportAutoSignEntry(@Path("id") id: Long): ApiResponse<String>

    @POST("/api/sport/auto-sign/queue/current")
    suspend fun currentSportAutoSignQueues(): ApiResponse<List<SportAutoSignQueue>>

    @GET("/api/sport/auto-sign/entry/{id}/mark-satisfied")
    suspend fun markSportAutoSignEntrySatisfied(@Path("id") id: Long): ApiResponse<String>

    // endregion auto sign

    // endregion sport
}