package dev.alllexey.itmowidgets.core.utils

import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import dev.alllexey.itmowidgets.core.model.resources.*

class SubjectLinkModelsTypeAdapterFactory : StrictResourceObjectTypeAdapterFactory(mapOf(
    SubjectLink::class.java to mapOf("id" to 's', "subjectId" to 'n', "subjectName" to 's', "periodKey" to 's', "category" to 's',
        "url" to 's', "visibility" to 's', "status" to 's', "score" to 'i', "myVote" to 'i', "isMine" to 'b', "isSaved" to 'b',
        "reportedByMe" to 'b', "updatedAt" to 's'),
    SubjectLinksResponse::class.java to mapOf("mine" to 'a', "shared" to 'a', "previous" to 'a', "audiences" to 'a', "premoderation" to 'b'),
    LinkAudience::class.java to mapOf("visibility" to 's', "label" to 's'),
    SubjectLinkRevision::class.java to mapOf("id" to 's', "linkId" to 's', "number" to 'i', "category" to 's', "url" to 's',
        "visibility" to 's', "status" to 's', "submittedAt" to 's'),
    SaveSubjectLinkRequest::class.java to mapOf("subjectId" to 'n', "subjectName" to 's', "periodKey" to 's', "category" to 's',
        "url" to 's', "visibility" to 's'),
    SetLinkSavedRequest::class.java to mapOf("saved" to 'b'),
    PinSubjectLinkRequest::class.java to mapOf("periodKey" to 's'),
    ResourceVoteRequest::class.java to mapOf("value" to 'i'),
)) {
    override fun validateJson(type: Class<*>, json: JsonObject) {
        if (type == SubjectLink::class.java && json.get("myVote").asInt !in -1..1) throw JsonParseException("Invalid vote")
        if (type == SubjectLinkRevision::class.java && json.get("number").asInt < 1) throw JsonParseException("Invalid revision number")
    }
}
