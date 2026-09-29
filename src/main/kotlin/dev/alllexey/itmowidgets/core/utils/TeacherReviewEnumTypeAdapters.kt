package dev.alllexey.itmowidgets.core.utils

import dev.alllexey.itmowidgets.core.model.reviews.ReviewRevisionStatus
import dev.alllexey.itmowidgets.core.model.reviews.ReviewVerification
import dev.alllexey.itmowidgets.core.model.reviews.SummaryConfidence
import dev.alllexey.itmowidgets.core.model.reviews.SummaryLevel
import dev.alllexey.itmowidgets.core.model.reviews.SummaryScaleKind
import dev.alllexey.itmowidgets.core.model.reviews.SummaryScaleValue
import dev.alllexey.itmowidgets.core.model.reviews.TeacherReviewKind
import dev.alllexey.itmowidgets.core.model.reviews.TeacherReviewStatus

class TeacherReviewKindTypeAdapter : StrictResourceEnumTypeAdapter<TeacherReviewKind>(TeacherReviewKind.entries)

class TeacherReviewStatusTypeAdapter : StrictResourceEnumTypeAdapter<TeacherReviewStatus>(TeacherReviewStatus.entries)

class ReviewRevisionStatusTypeAdapter : StrictResourceEnumTypeAdapter<ReviewRevisionStatus>(ReviewRevisionStatus.entries)

class ReviewVerificationTypeAdapter : StrictResourceEnumTypeAdapter<ReviewVerification>(ReviewVerification.entries)

class SummaryLevelTypeAdapter : StrictResourceEnumTypeAdapter<SummaryLevel>(SummaryLevel.entries)

class SummaryConfidenceTypeAdapter : StrictResourceEnumTypeAdapter<SummaryConfidence>(SummaryConfidence.entries)

class SummaryScaleKindTypeAdapter : StrictResourceEnumTypeAdapter<SummaryScaleKind>(SummaryScaleKind.entries)

class SummaryScaleValueTypeAdapter : StrictResourceEnumTypeAdapter<SummaryScaleValue>(SummaryScaleValue.entries)
