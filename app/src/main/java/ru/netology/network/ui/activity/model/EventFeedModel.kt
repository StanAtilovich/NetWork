package ru.netology.network.ui.activity.model

import ru.netology.network.ui.activity.dto.Event

data class EventFeedModel(
    val events: List<Event> = emptyList(),
    val empty: Boolean = false
)
