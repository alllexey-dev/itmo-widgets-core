package dev.alllexey.itmowidgets.core.utils

import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import dev.alllexey.itmowidgets.core.model.resources.*

class ModerationModelsTypeAdapterFactory : StrictResourceObjectTypeAdapterFactory(mapOf(
    ModerationCase::class.java to mapOf("id" to 's', "targetType" to 's', "status" to 's', "reason" to 's', "openedAt" to 's', "decisions" to 'a'),
    SubjectLinkTarget::class.java to mapOf("revision" to 'o', "link" to 'o', "author" to 'o', "reports" to 'a', "submitterHistory" to 'o'),
    SubmitterHistory::class.java to mapOf("approved" to 'n', "rejected" to 'n', "dismissedReports" to 'n', "activeRestrictions" to 'a'),
    ModerationDecision::class.java to mapOf("id" to 's', "actor" to 's', "action" to 's', "createdAt" to 's'),
    ModerationSettings::class.java to mapOf("policies" to 'm'),
    ModerationReport::class.java to mapOf("reason" to 's', "createdAt" to 's'),
    ModerationReportRequest::class.java to mapOf("reason" to 's'),
    RestrictionRequest::class.java to mapOf("capability" to 's'),
    ModerationDecisionRequest::class.java to mapOf("action" to 's'),
)) {
    override fun validateJson(type: Class<*>, json: JsonObject) {
        fun present(name: String) = json.get(name)?.takeUnless { it.isJsonNull }
        if (type == ModerationDecision::class.java) {
            when (json.get("actor").asString) {
                "MODERATOR" -> if (present("moderatorId")?.let { it.isJsonPrimitive && it.asJsonPrimitive.isString } != true) throw JsonParseException("Moderator ID required")
                "POLICY" -> if (present("moderatorId") != null || json.get("action").asString != "APPROVE") throw JsonParseException("Invalid policy decision")
            }
        }
    }
}
