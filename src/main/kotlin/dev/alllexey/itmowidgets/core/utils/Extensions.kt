package dev.alllexey.itmowidgets.core.utils

import api.myitmo.model.schedule.Lesson
import dev.alllexey.itmowidgets.core.model.LessonDto
import java.time.LocalDate
import java.time.LocalTime

fun Lesson.toDto(date: LocalDate): LessonDto {
    return LessonDto(
        pairId = pairId,
        date = date,
        start = LocalTime.parse(timeStart),
        end = LocalTime.parse(timeEnd),
        type = workType,
        typeId = workTypeId,
        note = note,
        subjectName = subject ?: "Неизвестный предмет",
        subjectId = subjectId,
        groupName = group,
        flowId = flowId.toLong(),
        flowTypeId = flowTypeId,
        teacherIsu = teacherId,
        teacherFio = teacherName,
        room = room,
        building = building,
        buildingId = bldId,
        mainBuildingId = mainBldId,
        format = format,
        formatId = formatId
    )
}