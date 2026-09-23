package dev.alllexey.itmowidgets.core.utils

import dev.alllexey.itmowidgets.core.model.resources.ModerationPolicy

class ModerationPolicyTypeAdapterFactory : StrictResourceObjectTypeAdapterFactory(mapOf(
    ModerationPolicy::class.java to mapOf("premoderation" to 'b', "reportThreshold" to 'i', "voteThreshold" to 'i', "dailySubmissionLimit" to 'i', "dailyReportLimit" to 'i'),
))
