package dev.alllexey.itmowidgets.core.model.reviews

import java.time.Instant

/** The overall tone of the reviews behind a summary. */
enum class SummaryLevel { VERY_NEGATIVE, NEGATIVE, MIXED, POSITIVE, VERY_POSITIVE }

/** How much the reviews agree; always `LOW` when fewer than five reviews went in. */
enum class SummaryConfidence { LOW, MEDIUM, HIGH }

/** The five scales in display order. */
enum class SummaryScaleKind { EXPLAINS, ATTITUDE, FAIRNESS, STRICTNESS, WORKLOAD }

enum class SummaryScaleValue { LOW, MEDIUM, HIGH, NOT_ENOUGH_DATA }

/**
 * The AI summary shown in a teacher's reviews. [reviewCount] is the number of reviews the shown summary was built
 * from, at least 3; it may lag behind the current reviews until a new summary is built. [tags] are codes of
 * Backend's fixed list kept as strings, so a tag added later does not break older clients; clients skip codes they
 * do not know. [scales] holds all five scales in [SummaryScaleKind] order.
 */
data class TeacherSummary(
    val reviewCount: Int,
    val description: String,
    val pros: List<String>,
    val cons: List<String>,
    val tags: List<String>,
    val scales: List<TeacherSummaryScale>,
    val level: SummaryLevel,
    val confidence: SummaryConfidence,
    val generatedAt: Instant,
)

/** [reason] is null when [value] is `NOT_ENOUGH_DATA`. */
data class TeacherSummaryScale(val kind: SummaryScaleKind, val value: SummaryScaleValue, val reason: String?)

/**
 * `GET /api/teachers/summary-levels` item: the tone of a shown summary whose confidence is `MEDIUM` or `HIGH`.
 * Teachers without such a summary are absent from the reply.
 */
data class TeacherSummaryLevel(val teacherIsu: Int, val level: SummaryLevel)
