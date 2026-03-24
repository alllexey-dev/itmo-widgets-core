package dev.alllexey.itmowidgets.core.utils

import api.myitmo.model.schedule.Lesson
import dev.alllexey.itmowidgets.core.model.LessonData
import java.time.LocalDate
import java.time.LocalTime

fun Lesson.toDto(date: LocalDate): LessonData {
    return LessonData(
        date = date,
        pairId = pairId,
        subjectId = subjectId,
        subject = subject,
        teacherId = teacherId,
        teacherName = teacherName,
        timeStart = LocalTime.parse(timeStart),
        timeEnd = LocalTime.parse(timeEnd),
        workTypeId = workTypeId,
        note = note,
        room = room,
        building = building
    )
}