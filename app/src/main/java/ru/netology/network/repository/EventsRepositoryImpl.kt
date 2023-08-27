package ru.netology.network.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.network.api.ApiService
import ru.netology.network.dao.EventDao
import ru.netology.network.dao.UserDao
import ru.netology.network.dto.Attachment
import ru.netology.network.dto.Event
import ru.netology.network.dto.EventRequest
import ru.netology.network.dto.MediaRequest
import ru.netology.network.dto.MediaResponse
import ru.netology.network.dto.Users
import ru.netology.network.dto.toEvent
import ru.netology.network.entity.EventEntity
import ru.netology.network.entity.toDto
import ru.netology.network.entity.toEntity
import ru.netology.network.enumeration.AttachmentType
import ru.netology.network.error.ApiError
import ru.netology.network.error.AppError
import ru.netology.network.error.NetworkError
import ru.netology.network.entity.UserEntity
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class EventsRepositoryImpl @Inject constructor(
    private val eventdao: EventDao,
    private val apiService: ApiService,
    private val userdao: UserDao,
) : EventsRepository {
    override val events = eventdao.getEvents()
        .map(List<EventEntity>::toDto)
        .flowOn(Dispatchers.Default)


    override suspend fun getEvents() {
        try {
            val response = apiService.getEvents()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val bodyResponse =
                response.body() ?: throw ApiError(response.code(), response.message())
            val users = bodyResponse.map {
                it.users?.map {
                    Users(
                        it.key.toLong(),
                        it.value.name,
                        it.value.avatar
                    )
                }
            }
            users.map {
                if (it != null) {
                    userdao.insert(it.toEntity())
                }
            }
            val event = bodyResponse.map {
                it.toEvent()
            }
            eventdao.insert(event.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw ru.netology.network.error.UnknownError
        }
    }


    suspend fun upload(upload: MediaRequest): MediaResponse {
        try {
            val media = MultipartBody.Part.createFormData(
                "file", upload.file.name, upload.file.asRequestBody()
            )

            val response = apiService.upload(media)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw ru.netology.network.error.UnknownError
        }
    }

    override suspend fun saveEvent(event: Event) {
        try {
            val eventRequest = EventRequest(
                event.id,
                event.content,
                event.datetime,
                event.coords,
                event.type,
                event.attachment,
                event.link,
                event.speakerIds
            )
            val response = apiService.saveEvent(eventRequest)

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val bodyResponse =
                response.body() ?: throw ApiError(response.code(), response.message())
            val eventResponse = Event(
                bodyResponse.id,
                bodyResponse.authorId,
                bodyResponse.author,
                bodyResponse.authorAvatar,
                bodyResponse.authorJob,
                bodyResponse.content,
                bodyResponse.datetime,
                bodyResponse.published,
                bodyResponse.coords,
                bodyResponse.type,
                bodyResponse.likeOwnerIds,
                bodyResponse.likedByMe,
                bodyResponse.speakerIds,
                bodyResponse.speakerIds?.map { id ->
                    bodyResponse.users?.get(id.toString())!!.name
                },
                bodyResponse.participantsIds,
                bodyResponse.participantsIds?.map { id ->
                    bodyResponse.users?.get(id.toString())!!.name
                },
                bodyResponse.participatedByMe,
                bodyResponse.attachment,
                bodyResponse.link,
                bodyResponse.ownedByMe
            )
            val users =
                bodyResponse.users?.map {
                    Users(
                        it.key.toLong(),
                        it.value.name,
                        it.value.avatar
                    )
                }
            eventdao.insert(EventEntity.fromDto(eventResponse))
            users?.map {
                userdao.insert(UserEntity.fromDto(it))
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw ru.netology.network.error.UnknownError
        }
    }

    override suspend fun saveEventWithAttachment(event: Event, upload: MediaRequest) {
        try {
            val media = upload(upload)
            val eventWithAttachment =
                event.copy(attachment = Attachment(media.url, AttachmentType.IMAGE))
            saveEvent(eventWithAttachment)
        } catch (e: AppError) {
            throw e
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw ru.netology.network.error.UnknownError
        }
    }

    override suspend fun likeEventById(id: Long, likedByMe: Boolean) {
        try {
            if (likedByMe) {
                val response = apiService.dislikeEventById(id)
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                val event =
                    response.body()?.toEvent() ?: throw ApiError(
                        response.code(),
                        response.message()
                    )
                eventdao.insert(EventEntity.fromDto(event))
            } else {
                val response = apiService.likeEventById(id)
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                val event =
                    response.body()?.toEvent() ?: throw ApiError(
                        response.code(),
                        response.message()
                    )
                eventdao.insert(EventEntity.fromDto(event))
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw ru.netology.network.error.UnknownError
        }
    }

    override suspend fun removeEventById(id: Long) {
        try {
            eventdao.removeById(id)
            val response = apiService.removeEventById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw ru.netology.network.error.UnknownError
        }
    }

    override suspend fun partEventById(id: Long, participatedByMe: Boolean) {
        try {
            if (participatedByMe) {
                val response = apiService.nonPartEventById(id)
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                val event =
                    response.body()?.toEvent() ?: throw ApiError(
                        response.code(),
                        response.message()
                    )
                eventdao.insert(EventEntity.fromDto(event))
            } else {
                val response = apiService.partEventById(id)
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                val event =
                    response.body()?.toEvent() ?: throw ApiError(
                        response.code(),
                        response.message()
                    )
                eventdao.insert(EventEntity.fromDto(event))
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw ru.netology.network.error.UnknownError
        }
    }
}