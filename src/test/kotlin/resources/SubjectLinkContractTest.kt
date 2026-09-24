package resources

import api.myitmo.MyItmo
import com.google.gson.JsonNull
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import dev.alllexey.itmowidgets.core.ItmoWidgetsImpl
import dev.alllexey.itmowidgets.core.model.resources.*
import kotlin.test.*

class SubjectLinkContractTest {
    private val gson = ItmoWidgetsImpl(MyItmo()).gson
    private val fixtures = ResourceContractFixtures

    @Test
    fun `every public link and moderation DTO round trips`() {
        val values = listOf(fixtures.link, fixtures.ownLink, fixtures.previousLink, fixtures.audience, fixtures.links,
            fixtures.links.copy(pinnedId = null, mine = emptyList(), audiences = emptyList()), fixtures.revision, fixtures.pendingRevision,
            fixtures.flowRevision, fixtures.save, fixtures.save.copy(title = "Очередь", visibility = LinkVisibility.PRIVATE, flowId = null), SetLinkSavedRequest(false),
            PinSubjectLinkRequest("2026-1", fixtures.id), PinSubjectLinkRequest("2026-1"), ResourceVoteRequest(-1),
            fixtures.restriction, fixtures.report, fixtures.decision, fixtures.history, fixtures.target, fixtures.case,
            fixtures.case.copy(target = null), fixtures.settings, ModerationPolicy(false, 5, -4, 8, 12),
            RestrictionRequest(RestrictionCapability.ALL, 7),
            ModerationDecisionRequest(ModerationAction.RESTRICT_USER, "Правила", RestrictionRequest(RestrictionCapability.REPORT)),
            ModerationReportRequest(ReportReason.OTHER, null))
        for (value in values) assertEquals(value, gson.fromJson(gson.toJson(value), value.javaClass), value.javaClass.simpleName)
    }

    @Test
    fun `link models use the exact wire field names`() {
        assertEquals(setOf("id", "subjectId", "subjectName", "periodKey", "category", "url", "title", "visibility", "flowId",
            "audienceLabel", "status", "reviewNote", "score", "myVote", "isMine", "isSaved", "reportedByMe", "author", "updatedAt"),
            gson.toJsonTree(fixtures.link.copy(reviewNote = "Проверено")).asJsonObject.keySet())
        assertEquals(setOf("mine", "shared", "previous", "pinnedId", "audiences", "premoderation"),
            gson.toJsonTree(fixtures.links).asJsonObject.keySet())
        assertEquals(setOf("flowId", "label", "typeId", "depth"), gson.toJsonTree(fixtures.audience).asJsonObject.keySet())
        assertEquals(setOf("id", "linkId", "number", "category", "url", "title", "visibility", "flowId", "status", "submittedAt",
            "decidedAt", "note"), gson.toJsonTree(fixtures.flowRevision).asJsonObject.keySet())
        assertEquals(setOf("targetType", "revision", "link", "author", "reports", "submitterHistory"), gson.toJsonTree(fixtures.target).asJsonObject.keySet())
        assertEquals(JsonParser.parseString("""{"subjectId":42,"subjectName":"Предмет","periodKey":"2026-1","category":"QUEUE",
            "url":"https://example.org/queue","visibility":"FLOW","flowId":7103}"""), gson.toJsonTree(fixtures.save))
        assertEquals(JsonParser.parseString("""{"subjectId":42,"subjectName":"Предмет","periodKey":"2026-1","category":"QUEUE",
            "url":"https://example.org/queue","visibility":"PRIVATE"}"""),
            gson.toJsonTree(fixtures.save.copy(visibility = LinkVisibility.PRIVATE, flowId = null)))
        assertEquals(JsonParser.parseString("""{"saved":true}"""), gson.toJsonTree(SetLinkSavedRequest(true)))
        assertEquals(JsonParser.parseString("""{"periodKey":"2026-1","linkId":"${fixtures.id}"}"""),
            gson.toJsonTree(PinSubjectLinkRequest("2026-1", fixtures.id)))
        assertEquals(JsonParser.parseString("""{"value":-1}"""), gson.toJsonTree(ResourceVoteRequest(-1)))
        val target = gson.toJsonTree(fixtures.case).asJsonObject["target"].asJsonObject
        assertEquals("SUBJECT_RESOURCE", target["targetType"].asString)
    }

    @Test
    fun `optional link fields decode from both null and absence`() {
        val optional = listOf("title", "flowId", "audienceLabel", "reviewNote", "author")
        val publicLink = fixtures.link.copy(visibility = LinkVisibility.ALL)
        val explicitNulls = gson.toJsonTree(publicLink).asJsonObject.apply { optional.forEach { add(it, JsonNull.INSTANCE) } }
        val expected = publicLink.copy(title = null, flowId = null, audienceLabel = null, reviewNote = null, author = null)
        assertEquals(expected, gson.fromJson(explicitNulls, SubjectLink::class.java))
        val absent = gson.toJsonTree(publicLink).asJsonObject.apply { optional.forEach { remove(it) } }
        assertEquals(expected, gson.fromJson(absent, SubjectLink::class.java))
        val privateSave = gson.toJsonTree(fixtures.save.copy(visibility = LinkVisibility.PRIVATE, flowId = null)).asJsonObject
        assertNull(gson.fromJson(privateSave.apply { add("flowId", JsonNull.INSTANCE) }, SaveSubjectLinkRequest::class.java).flowId)
        val noPin = gson.toJsonTree(fixtures.links).asJsonObject.apply { add("pinnedId", JsonNull.INSTANCE) }
        assertNull(gson.fromJson(noPin, SubjectLinksResponse::class.java).pinnedId)
    }

    @Test
    fun `strict enums reject unknown null numeric and malformed values while future restriction blocks all`() {
        for (type in listOf(LinkCategory::class.java, LinkVisibility::class.java, SubjectLinkStatus::class.java, LinkRevisionStatus::class.java,
            ReportReason::class.java, ModerationAction::class.java, ModerationTargetType::class.java, ModerationCaseStatus::class.java,
            ModerationCaseReason::class.java, ModerationActor::class.java)) {
            for (value in type.enumConstants) assertEquals(value, gson.fromJson(gson.toJson(value), type))
            for (wire in listOf("\"UNKNOWN\"", "null", "0", "3", "true", "{}", "[]")) {
                assertFailsWith<JsonParseException>("${type.simpleName}: $wire") { gson.fromJson(wire, type) }
            }
        }
        for ((field, wire) in listOf("category" to "\"UNKNOWN\"", "visibility" to "3", "status" to "null")) {
            val json = gson.toJsonTree(fixtures.link).asJsonObject.apply { add(field, JsonParser.parseString(wire)) }
            assertFailsWith<JsonParseException>("SubjectLink.$field: $wire") { gson.fromJson(json, SubjectLink::class.java) }
        }
        for (wire in listOf("\"GROUP\"", "\"FLOW_ALL\"")) {
            val json = gson.toJsonTree(fixtures.link).asJsonObject.apply { add("visibility", JsonParser.parseString(wire)) }
            assertFailsWith<JsonParseException>("SubjectLink.visibility: $wire") { gson.fromJson(json, SubjectLink::class.java) }
        }
        assertEquals(RestrictionCapability.ALL, gson.fromJson("\"FUTURE_CAPABILITY\"", RestrictionCapability::class.java))
        for (wire in listOf("null", "0", "false", "{}")) {
            assertFailsWith<JsonParseException> { gson.fromJson(wire, RestrictionCapability::class.java) }
        }
    }

    @Test
    fun `required link response restriction and policy fields never become JVM defaults`() {
        val required = listOf(
            Triple(fixtures.link as Any, SubjectLink::class.java, listOf("id", "subjectId", "subjectName", "periodKey", "category", "url",
                "visibility", "status", "score", "myVote", "isMine", "isSaved", "reportedByMe", "updatedAt")),
            Triple(fixtures.links as Any, SubjectLinksResponse::class.java, listOf("mine", "shared", "previous", "audiences", "premoderation")),
            Triple(fixtures.audience as Any, LinkAudience::class.java, listOf("flowId", "label", "typeId", "depth")),
            Triple(fixtures.revision as Any, SubjectLinkRevision::class.java, listOf("id", "linkId", "number", "category", "url",
                "visibility", "status", "submittedAt")),
            Triple(fixtures.target as Any, SubjectLinkTarget::class.java, listOf("revision", "link", "author", "reports", "submitterHistory")),
            Triple(fixtures.save as Any, SaveSubjectLinkRequest::class.java, listOf("subjectId", "subjectName", "periodKey", "category", "url", "visibility")),
            Triple(SetLinkSavedRequest(true) as Any, SetLinkSavedRequest::class.java, listOf("saved")),
            Triple(PinSubjectLinkRequest("2026-1") as Any, PinSubjectLinkRequest::class.java, listOf("periodKey")),
            Triple(fixtures.restriction as Any, UserRestriction::class.java, listOf("id", "capability", "reason", "startsAt")),
            Triple(ModerationPolicy() as Any, ModerationPolicy::class.java, listOf("premoderation", "reportThreshold", "voteThreshold",
                "dailySubmissionLimit", "dailyReportLimit")),
        )
        for ((model, type, names) in required) for (name in names) {
            val missing = gson.toJsonTree(model).asJsonObject.apply { remove(name) }
            assertFailsWith<JsonParseException>("${type.simpleName}.$name missing") { gson.fromJson(missing, type) }
            val nullValue = gson.toJsonTree(model).asJsonObject.apply { add(name, JsonNull.INSTANCE) }
            assertFailsWith<JsonParseException>("${type.simpleName}.$name null") { gson.fromJson(nullValue, type) }
        }
        for (wire in listOf("\"0\"", "1.5", "2147483648", "true")) {
            val malformed = gson.toJsonTree(fixtures.link).asJsonObject.apply { add("score", JsonParser.parseString(wire)) }
            assertFailsWith<JsonParseException> { gson.fromJson(malformed, SubjectLink::class.java) }
        }
        for (flag in listOf("isMine", "isSaved", "reportedByMe")) {
            val malformed = gson.toJsonTree(fixtures.link).asJsonObject.apply { addProperty(flag, "true") }
            assertFailsWith<JsonParseException>(flag) { gson.fromJson(malformed, SubjectLink::class.java) }
        }
        for (vote in listOf(2, -2)) {
            val malformed = gson.toJsonTree(fixtures.link).asJsonObject.apply { addProperty("myVote", vote) }
            assertFailsWith<JsonParseException> { gson.fromJson(malformed, SubjectLink::class.java) }
        }
        val badRevision = gson.toJsonTree(fixtures.revision).asJsonObject.apply { addProperty("number", 0) }
        assertFailsWith<JsonParseException> { gson.fromJson(badRevision, SubjectLinkRevision::class.java) }
        for ((field, wire) in listOf("flowId" to "\"7103\"", "flowId" to "1.5", "flowId" to "true", "depth" to "0", "typeId" to "\"3\"")) {
            val malformed = gson.toJsonTree(fixtures.audience).asJsonObject.apply { add(field, JsonParser.parseString(wire)) }
            assertFailsWith<JsonParseException>("LinkAudience.$field: $wire") { gson.fromJson(malformed, LinkAudience::class.java) }
        }
        val duplicate = gson.toJson(fixtures.links).dropLast(1) + ",\"premoderation\":false}"
        assertFailsWith<JsonParseException> { gson.fromJson(duplicate, SubjectLinksResponse::class.java) }
        for (list in listOf("mine", "shared", "previous", "audiences")) {
            val withNull = gson.toJsonTree(fixtures.links).asJsonObject.apply { add(list, JsonParser.parseString("[null]")) }
            assertFailsWith<JsonParseException>(list) { gson.fromJson(withNull, SubjectLinksResponse::class.java) }
        }
        val badPin = gson.toJsonTree(fixtures.links).asJsonObject.apply { addProperty("pinnedId", "not-a-uuid") }
        assertFailsWith<JsonParseException> { gson.fromJson(badPin, SubjectLinksResponse::class.java) }
        assertFailsWith<JsonParseException> { gson.fromJson("""{"policies":{"SUBJECT_RESOURCE":null}}""", ModerationSettings::class.java) }
    }

    @Test
    fun `a flow id travels exactly with FLOW visibility`() {
        val cases = listOf(
            gson.toJsonTree(fixtures.link).asJsonObject to SubjectLink::class.java,
            gson.toJsonTree(fixtures.flowRevision).asJsonObject to SubjectLinkRevision::class.java,
            gson.toJsonTree(fixtures.save).asJsonObject to SaveSubjectLinkRequest::class.java,
        )
        for ((json, type) in cases) {
            assertEquals(7103L, (gson.fromJson(json, type) as Any).let {
                when (it) { is SubjectLink -> it.flowId; is SubjectLinkRevision -> it.flowId; else -> (it as SaveSubjectLinkRequest).flowId }
            })
            for (wire in listOf("null", "\"7103\"", "1.5", "9223372036854775808", "{}")) {
                val malformed = json.deepCopy().apply { add("flowId", JsonParser.parseString(wire)) }
                assertFailsWith<JsonParseException>("${type.simpleName}.flowId: $wire") { gson.fromJson(malformed, type) }
            }
            assertFailsWith<JsonParseException>("${type.simpleName} without flowId") {
                gson.fromJson(json.deepCopy().apply { remove("flowId") }, type)
            }
            for (visibility in listOf("PRIVATE", "ALL")) {
                val widened = json.deepCopy().apply { addProperty("visibility", visibility) }
                assertFailsWith<JsonParseException>("${type.simpleName} $visibility with flowId") { gson.fromJson(widened, type) }
            }
        }
    }

    @Test
    fun `automatic decisions require a policy actor without a fictional moderator`() {
        val automatic = fixtures.decision.copy(actor = ModerationActor.POLICY, moderatorId = null, action = ModerationAction.APPROVE)
        assertEquals(automatic, gson.fromJson(gson.toJson(automatic), ModerationDecision::class.java))
        for (invalid in listOf(automatic.copy(moderatorId = fixtures.id), automatic.copy(action = ModerationAction.REJECT),
            fixtures.decision.copy(moderatorId = null))) {
            assertFailsWith<JsonParseException> { gson.fromJson(gson.toJson(invalid), ModerationDecision::class.java) }
        }
        val missing = gson.toJsonTree(automatic).asJsonObject.apply { remove("actor") }
        assertFailsWith<JsonParseException> { gson.fromJson(missing, ModerationDecision::class.java) }
    }

    @Test
    fun `link moderation target decodes polymorphically under SUBJECT_RESOURCE and rejects unknown targets`() {
        val json = gson.toJson(fixtures.target, ModerationCaseTarget::class.java)
        assertEquals("SUBJECT_RESOURCE", JsonParser.parseString(json).asJsonObject["targetType"].asString)
        assertEquals(fixtures.target, assertIs<SubjectLinkTarget>(gson.fromJson(json, ModerationCaseTarget::class.java)))
        assertEquals(fixtures.case, gson.fromJson(gson.toJson(fixtures.case), ModerationCase::class.java))
        for (target in listOf("UNKNOWN", "TEACHER_REVIEW")) {
            assertFailsWith<JsonParseException> {
                gson.fromJson(json.replace("SUBJECT_RESOURCE", target), ModerationCaseTarget::class.java)
            }
        }
        assertFailsWith<JsonParseException> { gson.fromJson("{}", ModerationCaseTarget::class.java) }
        val noRevision = JsonParser.parseString(json).asJsonObject.apply { remove("revision") }
        assertFailsWith<JsonParseException> { gson.fromJson(noRevision, ModerationCaseTarget::class.java) }
    }
}
