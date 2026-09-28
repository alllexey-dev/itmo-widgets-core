package dev.alllexey.itmowidgets.core.model.reviews

import java.time.LocalDate
import java.util.UUID

/** Anonymous external reviews for one teacher, ordered by Backend from newest to oldest. */
data class TeacherReviewsResponse(
    val teacherIsu: Int,
    val providerUrl: String,
    val external: List<ExternalTeacherReview>,
)

/** A provider's review without author details; at most one of the two date fields is present. */
data class ExternalTeacherReview(
    val id: UUID,
    val subjectTitle: String?,
    val writtenOn: LocalDate?,
    val writtenBeforeYear: Int?,
    val sourceTitle: String?,
    val sourceLink: String?,
    val text: String,
)
