package dev.alllexey.itmowidgets.core

import dev.alllexey.itmowidgets.core.model.*
import dev.alllexey.itmowidgets.core.model.social.UserProfile
import dev.alllexey.itmowidgets.core.model.social.UserLookupRequest
import dev.alllexey.itmowidgets.core.model.social.UserLookupResponse
import retrofit2.http.*
import java.time.LocalDate

interface ItmoWidgetsApi {

    // region devices

    @POST("/api/device/register-device")
    suspend fun registerDevice(@Body request: RegisterDeviceRequest): ApiResponse<String>

    @HTTP(method = "DELETE", path = "/api/device/current", hasBody = true)
    suspend fun unregisterCurrentDevice(
        @Body request: UnregisterDeviceRequest
    ): ApiResponse<String>

    // endregion devices

    // region app

    @GET("/api/app/version")
    suspend fun latestAppVersion(): ApiResponse<String>

    /** Application release metadata; the legacy string endpoint remains unchanged. */
    @GET("/api/app/version-info")
    suspend fun appVersionInfo(): ApiResponse<AppVersionInfo>

    // endregion app

    // region schedule

    @POST("/api/schedule/lessons/sync")
    suspend fun syncLessons(@Body lessonSyncRequest: LessonSyncRequest): ApiResponse<String>

    @GET("/api/schedule/lessons/user/{isu}")
    suspend fun userLessons(
        @Path("isu") isu: Int,
        @Query("from") from: LocalDate,
        @Query("to") to: LocalDate
    ): ApiResponse<List<LessonDto>>

    @GET("/api/schedule/lessons/{pairId}/users")
    suspend fun usersByPairId(@Path("pairId") pairId: Long): ApiResponse<List<UserData>>

    // endregion schedule

    // region friend

    /** A crossed request accepts the incoming request. Successful actions return fresh capabilities. */
    @POST("/api/friends/{isu}/request")
    suspend fun sendFriendRequest(@Path("isu") isu: Int): ApiResponse<UserProfile>

    @POST("/api/friends/{isu}/accept")
    suspend fun acceptFriendRequest(@Path("isu") isu: Int): ApiResponse<UserProfile>

    @POST("/api/friends/{isu}/reject")
    suspend fun rejectFriendRequest(@Path("isu") isu: Int): ApiResponse<UserProfile>

    @POST("/api/friends/{isu}/cancel")
    suspend fun cancelFriendRequest(@Path("isu") isu: Int): ApiResponse<UserProfile>

    @DELETE("/api/friends/{isu}")
    suspend fun removeFriend(@Path("isu") isu: Int): ApiResponse<UserProfile>

    @GET("/api/friends")
    suspend fun friends(): ApiResponse<List<UserProfile>>

    @GET("/api/friends/requests/incoming")
    suspend fun incomingFriendRequests(): ApiResponse<List<UserProfile>>

    @GET("/api/friends/requests/outgoing")
    suspend fun outgoingFriendRequests(): ApiResponse<List<UserProfile>>

    // endregion friend

    // region user

    @GET("/api/users/{isu}")
    suspend fun userProfile(@Path("isu") isu: Int): ApiResponse<UserProfile>

    @GET("/api/users/{isu}/friends")
    suspend fun userFriends(@Path("isu") isu: Int): ApiResponse<List<UserProfile>>

    @POST("/api/users/lookup")
    suspend fun lookupUsers(@Body request: UserLookupRequest): ApiResponse<UserLookupResponse>

    @GET("/api/users/me/privacy")
    suspend fun myPrivacySettings(): ApiResponse<UserPrivacySettings>

    @PUT("/api/users/me/privacy")
    suspend fun updateMyPrivacySettings(@Body privacySettings: UserPrivacySettings): ApiResponse<UserPrivacySettings>

    @PUT("/api/users/me/id-token")
    suspend fun updateIdTokenData(@Body idTokenRequest: IdTokenRequest): ApiResponse<String>

    @GET("/api/users/me/data")
    suspend fun myUserData(): ApiResponse<UserData>

    // endregion user

    // region sport

    @POST("/api/sport/sign/sync")
    suspend fun syncSportLessons(@Body lessonIds: List<Long>): ApiResponse<String>

    @GET("/api/sport/friends/sport-bookings")
    suspend fun friendsSportBookings(): ApiResponse<FriendsSportBookingsResponse>

    @GET("/api/sport/users/{isu}/bookings")
    suspend fun userSportBookings(@Path("isu") isu: Int): ApiResponse<UserSportBookingsResponse>

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
