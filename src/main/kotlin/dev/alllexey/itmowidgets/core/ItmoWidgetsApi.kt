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

    // region user

    @GET("/api/users/me/settings")
    suspend fun mySettings(): ApiResponse<UserSettings>

    @PUT("/api/users/me/settings")
    suspend fun updateMySettings(@Body userSettings: UserSettings): ApiResponse<UserSettings>

    @PUT("/api/users/me/id-token")
    suspend fun updateIdTokenData(@Body idTokenRequest: IdTokenRequest): ApiResponse<String>

    @GET("/api/users/me/data")
    suspend fun myUserData(): ApiResponse<UserData>

    // endregion user

    // region sport

    @POST("/api/sport/sign/sync")
    suspend fun syncSportLessons(@Body lessonIds: List<Long>): ApiResponse<String>

    // region free sign

    @GET("/api/sport/free-sign/entry/my")
    suspend fun mySportFreeSignEntries(): ApiResponse<List<SportFreeSignEntry>>

    @POST("/api/sport/free-sign/entry/create")
    suspend fun createSportFreeSignEntry(@Body request: SportFreeSignRequest): ApiResponse<SportFreeSignEntry>

    @POST("/api/sport/free-sign/entry/{id}/cancel")
    suspend fun cancelSportFreeSignEntry(@Path("id") id: Long): ApiResponse<String>

    @POST("/api/sport/free-sign/lesson/{lessonId}/cancel")
    suspend fun cancelSportFreeSignEntryByLesson(@Path("lessonId") lessonId: Long): ApiResponse<String>

    @GET("/api/sport/free-sign/queue/current")
    suspend fun currentSportFreeSignQueues(): ApiResponse<List<SportFreeSignQueue>>

    @POST("/api/sport/free-sign/entry/{id}/mark-satisfied")
    suspend fun markSportFreeSignEntrySatisfied(@Path("id") id: Long): ApiResponse<String>

    @POST("/api/sport/free-sign/lesson/{lessonId}/mark-satisfied")
    suspend fun markSportFreeSignEntrySatisfiedByLesson(@Path("lessonId") id: Long): ApiResponse<String>

    // endregion free sign

    // region auto sign

    @GET("/api/sport/auto-sign/limits")
    suspend fun sportAutoSignLimits(): ApiResponse<SportAutoSignLimits>

    @GET("/api/sport/auto-sign/entry/my")
    suspend fun mySportAutoSignEntries(): ApiResponse<List<SportAutoSignEntry>>

    @POST("/api/sport/auto-sign/entry/create")
    suspend fun createSportAutoSignEntry(@Body request: SportAutoSignRequest): ApiResponse<SportAutoSignEntry>

    @POST("/api/sport/auto-sign/entry/{id}/cancel")
    suspend fun cancelSportAutoSignEntry(@Path("id") id: Long): ApiResponse<String>

    // in this context "lesson" is the real lesson
    @POST("/api/sport/auto-sign/lesson/{lessonId}/cancel")
    suspend fun cancelSportAutoSignEntryByLesson(@Path("lessonId") lessonId: Long): ApiResponse<String>

    @POST("/api/sport/auto-sign/queue/current")
    suspend fun currentSportAutoSignQueues(): ApiResponse<List<SportAutoSignQueue>>

    @POST("/api/sport/auto-sign/entry/{id}/mark-satisfied")
    suspend fun markSportAutoSignEntrySatisfied(@Path("id") id: Long): ApiResponse<String>

    // in this context "lesson" is the real lesson
    @POST("/api/sport/auto-sign/lesson/{lessonId}/mark-satisfied")
    suspend fun markSportAutoSignEntrySatisfiedByLesson(@Path("lessonId") lessonId: Long): ApiResponse<String>

    // endregion auto sign

    // endregion sport
}