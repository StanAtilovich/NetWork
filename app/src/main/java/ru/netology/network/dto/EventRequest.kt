package ru.netology.network.dto

import ru.netology.network.enumeration.EventType


data class EventRequest(
    val id: Long,
    val content: String,
    val datetime: String?,
    val coords: Coordinates?,
    val type: EventType?,
    val attachment: Attachment?,
    val link: String?,
    val speakerIds: List<Long>?
)