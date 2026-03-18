package dev.alllexey.itmowidgets.core

import dev.alllexey.itmowidgets.core.model.*
import retrofit2.http.*

interface ItmoWidgetsApi {

    // region devices

    @POST("/api/device/register-device")
    suspend fun registerDevice(@Body request: RegisterDeviceRequest): ApiResponse<String>

    // endregion devices

    // region app

    @GET("/api/app/version")
    suspend fun latestAppVersion(): ApiResponse<String>

    // endregion app

    // region free sign

    @GET("/api/sport/free-sign/entry/my")
    suspend fun mySportFreeSignEntries(): ApiResponse<List<SportFreeSignEntry>>

    @POST("/api/sport/free-sign/entry/create")
    suspend fun createSportFreeSignEntry(@Body request: SportFreeSignRequest): ApiResponse<SportFreeSignEntry>

    @DELETE("/api/sport/free-sign/entry/{id}")
    suspend fun deleteSportFreeSignEntry(@Path("id") id: Long): ApiResponse<String>

    @GET("/api/sport/free-sign/queue/current")
    suspend fun currentSportFreeSignQueues(): ApiResponse<List<SportFreeSignQueue>>

    @POST("/api/sport/free-sign/entry/{id}/mark-satisfied")
    suspend fun markSportFreeSignEntrySatisfied(@Path("id") id: Long): ApiResponse<String>

    // endregion free sign

    // region auto sign

    @GET("/api/sport/auto-sign/limits")
    suspend fun sportAutoSignLimits(): ApiResponse<SportAutoSignLimits>

    @GET("/api/sport/auto-sign/entry/my")
    suspend fun mySportAutoSignEntries(): ApiResponse<List<SportAutoSignEntry>>

    @POST("/api/sport/auto-sign/entry/create")
    suspend fun createSportAutoSignEntry(@Body request: SportAutoSignRequest): ApiResponse<SportAutoSignEntry>

    @DELETE("/api/sport/auto-sign/entry/{id}")
    suspend fun deleteSportAutoSignEntry(@Path("id") id: Long): ApiResponse<String>

    @POST("/api/sport/auto-sign/queue/current")
    suspend fun currentSportAutoSignQueues(): ApiResponse<List<SportAutoSignQueue>>

    @POST("/api/sport/auto-sign/entry/{id}/mark-satisfied")
    suspend fun markSportAutoSignEntrySatisfied(@Path("id") id: Long): ApiResponse<String>

    // endregion auto sign

    // endregion sport
}