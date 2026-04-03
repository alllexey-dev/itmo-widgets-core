package dev.alllexey.itmowidgets.core.model

import java.time.LocalDate
import java.time.LocalTime

data class RegisterDeviceRequest(val fcmToken: String, val deviceName: String)

data class IdTokenRequest(val idToken: String)

data class FriendRequest(val isu: Int)

data class LessonSyncRequest(val lessons: List<LessonDto>, val from: LocalDate, val to: LocalDate)

data class LessonDto(

    val pairId: Long,
    val date: LocalDate,
    val start: LocalTime,
    val end: LocalTime,
    val type: String, // Lesson#workType = Lesson#type
    /**
     * 1 - лекция
     * 2 - лабораторная
     * 3 - практика
     * 4 - ?
     * 5 - экзамен
     * 6 - зачёт
     * 7, 8, 9 - ?
     * 10 - консультация
     * 11 - спорт
     */
    val typeId: Int, // Lesson#workTypeId
    val note: String?,

    val subjectName: String,
    val subjectId: Long,
    val groupName: String,
    val flowId: Long,
    /**
     * 2 - пары
     * 3 - спорт
     * 5 - бронь кабинетов (?)
     */
    val flowTypeId: Int,

    val teacherIsu: Long?,
    val teacherFio: String?,

    val room: String?,
    val building: String?,
    val buildingId: Int?,
    /**
     * 13 - Кронва
     * 273 - Ломо
     * 5 - Вязьма
     * 319 - Виртуальные аудитории
     */
    val mainBuildingId: Int?,
    val format: String,
    val formatId: Int, // 1: Очный, 2: Очно - дистанционный, 3: Дистанционный
)

// region sport

data class SportFreeSignRequest(
    val lessonId: Long,
    val forceSign: Boolean,
)

data class SportAutoSignRequest(
    val prototypeLessonId: Long,
)

// endregion sport