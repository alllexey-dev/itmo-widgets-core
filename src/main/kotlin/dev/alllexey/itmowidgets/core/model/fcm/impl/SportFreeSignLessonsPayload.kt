package dev.alllexey.itmowidgets.core.model.fcm.impl

import dev.alllexey.itmowidgets.core.model.SportLessonDto
import dev.alllexey.itmowidgets.core.model.fcm.FcmPayload

data class SportFreeSignLessonsPayload(
    val sportLessons: List<SportLessonDto>
) : FcmPayload {

    override fun getType() = TYPE

    companion object {
        const val TYPE = "SPORT_FREE_SIGN_LESSONS_PAYLOAD"
    }
}