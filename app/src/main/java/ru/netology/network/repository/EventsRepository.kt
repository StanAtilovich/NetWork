package ru.netology.network.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.network.dto.Event
import ru.netology.network.dto.MediaRequest

interface EventsRepository {
    val events: Flow<List<Event>>

    suspend fun getEvents()
    suspend fun saveEvent(event: Event)
    suspend fun saveEventWithAttachment(event: Event, upload: MediaRequest)
    suspend fun likeEventById(id: Long, likedByMe: Boolean)
    suspend fun removeEventById(id: Long)
    suspend fun partEventById(id: Long, participatedByMe: Boolean)
}