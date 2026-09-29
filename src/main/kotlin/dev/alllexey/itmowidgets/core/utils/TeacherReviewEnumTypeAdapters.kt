package dev.alllexey.itmowidgets.core.utils

import dev.alllexey.itmowidgets.core.model.reviews.ReviewRevisionStatus
import dev.alllexey.itmowidgets.core.model.reviews.ReviewVerification
import dev.alllexey.itmowidgets.core.model.reviews.TeacherReviewKind
import dev.alllexey.itmowidgets.core.model.reviews.TeacherReviewStatus

class TeacherReviewKindTypeAdapter : StrictResourceEnumTypeAdapter<TeacherReviewKind>(TeacherReviewKind.entries)

class TeacherReviewStatusTypeAdapter : StrictResourceEnumTypeAdapter<TeacherReviewStatus>(TeacherReviewStatus.entries)

class ReviewRevisionStatusTypeAdapter : StrictResourceEnumTypeAdapter<ReviewRevisionStatus>(ReviewRevisionStatus.entries)

class ReviewVerificationTypeAdapter : StrictResourceEnumTypeAdapter<ReviewVerification>(ReviewVerification.entries)
