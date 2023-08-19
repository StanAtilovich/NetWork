package ru.netology.network.model

import ru.netology.network.dto.Event


data class EventFeedModel(
    val events: List<Event> = emptyList(),
    val empty: Boolean = false
)
