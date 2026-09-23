package dev.alllexey.itmowidgets.core

import dev.alllexey.itmowidgets.core.model.ApiResponse
import dev.alllexey.itmowidgets.core.model.resources.*
import retrofit2.http.*
import java.util.UUID

/** Moderator-only operations are deliberately separate from the ordinary application API. */
interface ItmoWidgetsModerationApi {
    @GET("/api/moderation/cases")
    suspend fun moderationCases(@Query("status") status: String = "OPEN"): ApiResponse<List<ModerationCase>>

    @POST("/api/moderation/cases/{id}/decisions")
    suspend fun decide(@Path("id") caseId: UUID, @Body request: ModerationDecisionRequest): ApiResponse<ModerationCase>

    @GET("/api/moderation/restrictions")
    suspend fun userRestrictions(@Query("isu") isu: Int): ApiResponse<List<UserRestriction>>

    @POST("/api/moderation/restrictions/{id}/revoke")
    suspend fun revokeRestriction(@Path("id") id: UUID): ApiResponse<Unit>

    @GET("/api/moderation/settings")
    suspend fun moderationSettings(): ApiResponse<ModerationSettings>

    @PUT("/api/moderation/settings")
    suspend fun updateModerationSettings(@Body request: ModerationSettings): ApiResponse<ModerationSettings>
}
