package dev.alllexey.itmowidgets.core.model.fcm.impl

import dev.alllexey.itmowidgets.core.model.fcm.FcmPayload

data class SportLessonsPayload(
    val sportLessonIds: List<Long>
) : FcmPayload {

    override fun getType() = TYPE

    companion object {
        const val TYPE = "SPORT_LESSONS_PAYLOAD"
    }
}