package dev.alllexey.itmowidgets.core.utils

import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import dev.alllexey.itmowidgets.core.model.resources.*

class SubjectLinkModelsTypeAdapterFactory : StrictResourceObjectTypeAdapterFactory(mapOf(
    SubjectLink::class.java to mapOf("id" to 's', "subjectId" to 'n', "subjectName" to 's', "periodKey" to 's', "category" to 's',
        "url" to 's', "visibility" to 's', "status" to 's', "score" to 'i', "myVote" to 'i', "isMine" to 'b',
        "reportedByMe" to 'b', "updatedAt" to 's'),
    SubjectLinksResponse::class.java to mapOf("mine" to 'a', "shared" to 'a', "previous" to 'a', "audiences" to 'a', "premoderation" to 'b'),
    LinkAudience::class.java to mapOf("flowId" to 'n', "label" to 's', "typeId" to 'i', "depth" to 'i'),
    SubjectLinkRevision::class.java to mapOf("id" to 's', "linkId" to 's', "number" to 'i', "category" to 's', "url" to 's',
        "visibility" to 's', "status" to 's', "submittedAt" to 's'),
    SaveSubjectLinkRequest::class.java to mapOf("subjectId" to 'n', "subjectName" to 's', "periodKey" to 's', "category" to 's',
        "url" to 's', "visibility" to 's'),
    PinSubjectLinkRequest::class.java to mapOf("periodKey" to 's'),
    ResourceVoteRequest::class.java to mapOf("value" to 'i'),
)) {
    override fun validateJson(type: Class<*>, json: JsonObject) {
        if (type == SubjectLink::class.java && json.get("myVote").asInt !in -1..1) throw JsonParseException("Invalid vote")
        if (type == SubjectLinkRevision::class.java && json.get("number").asInt < 1) throw JsonParseException("Invalid revision number")
        if (type == LinkAudience::class.java && json.get("depth").asInt < 1) throw JsonParseException("Invalid flow depth")
        if (type in FLOW_SCOPED) validateFlow(json)
    }

    /** A FLOW audience names exactly one flow; other visibilities carry none. */
    private fun validateFlow(json: JsonObject) {
        val flowId = json.get("flowId")?.takeUnless { it.isJsonNull }
        if (flowId != null && !(flowId.isJsonPrimitive && flowId.asJsonPrimitive.isNumber &&
                flowId.toString().matches(INTEGER) && runCatching { flowId.asBigDecimal.longValueExact() }.isSuccess)) {
            throw JsonParseException("Invalid flowId")
        }
        if ((json.get("visibility").asString == LinkVisibility.FLOW.name) != (flowId != null)) {
            throw JsonParseException("flowId must be present exactly for FLOW visibility")
        }
    }

    private companion object {
        val FLOW_SCOPED = setOf(SubjectLink::class.java, SubjectLinkRevision::class.java, SaveSubjectLinkRequest::class.java)
        val INTEGER = Regex("-?[0-9]+")
    }
}
