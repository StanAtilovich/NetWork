package ru.netology.network.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.network.api.ApiService
import ru.netology.network.auth.AuthState
import ru.netology.network.dao.PostDao
import ru.netology.network.dao.UserDao
import ru.netology.network.dto.Attachment
import ru.netology.network.dto.MediaRequest
import ru.netology.network.dto.MediaResponse
import ru.netology.network.dto.Post
import ru.netology.network.dto.PostRequest
import ru.netology.network.dto.Users
import ru.netology.network.dto.toPost
import ru.netology.network.enumeration.AttachmentType
import ru.netology.network.error.ApiError
import ru.netology.network.error.AppError
import ru.netology.network.error.NetworkError
import ru.netology.network.entity.PostEntity
import ru.netology.network.entity.UserEntity
import ru.netology.network.entity.toDto
import ru.netology.network.entity.toEntity
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val postdao: PostDao,
    private val userdao: UserDao,
    private val apiService: ApiService
) : PostRepository {
    override val posts = postdao.getPosts()
        .map(List<PostEntity>::toDto)
        .flowOn(Dispatchers.Default)
    override val users = userdao.getUsers()
        .map(List<UserEntity>::toDto)
        .flowOn(Dispatchers.Default)

    override suspend fun getPosts() {
        try {
            val response = apiService.getAll()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val bodyResponse =
                response.body() ?: throw ApiError(response.code(), response.message())
            val post = bodyResponse.map {
                it.toPost()
            }

            val users = bodyResponse.map {
                it.users?.map {
                    Users(
                        it.key.toLong(),
                        it.value.name,
                        it.value.avatar
                    )
                }
            }
            postdao.insert(post.toEntity())
            users.map {
                if (it != null) {
                    userdao.insert(it.toEntity())
                }
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw ru.netology.network.error.UnknownError
        }
    }

    override suspend fun getPostsByAuthor(userId: Long) {
        try {
            val response = apiService.getPostsByAuthor(userId)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val bodyResponse =
                response.body() ?: throw ApiError(response.code(), response.message())
            val post = bodyResponse.map {
                it.toPost()
            }
            val users = bodyResponse.map {
                it.users?.map {
                    Users(
                        it.key.toLong(),
                        it.value.name,
                        it.value.avatar
                    )
                }
            }
            postdao.insert(post.toEntity())
            users.map {
                if (it != null) {
                    userdao.insert(it.toEntity())
                }
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw ru.netology.network.error.UnknownError
        }
    }

    override suspend fun upload(upload: MediaRequest): MediaResponse {
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

    override suspend fun save(post: Post) {
        try {
            val postRequest = PostRequest(
                post.id, post.content,
                post.coords,
                post.link,
                post.attachment, post.mentionIds
            )
            val response = apiService.save(postRequest)

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val bodyResponse =
                response.body() ?: throw ApiError(response.code(), response.message())
            val postResponse = bodyResponse.toPost()
            val users =
                bodyResponse.users?.map {
                    Users(
                        it.key.toLong(),
                        it.value.name,
                        it.value.avatar
                    )
                }

            postdao.insert(PostEntity.fromDto(postResponse))
            users?.map {
                userdao.insert(UserEntity.fromDto(it))
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw ru.netology.network.error.UnknownError
        }
    }

    override suspend fun saveWithAttachment(post: Post, upload: MediaRequest) {
        try {
            val media = upload(upload)
            val postWithAttachment =
                post.copy(attachment = Attachment(media.url, AttachmentType.IMAGE))
            save(postWithAttachment)
        } catch (e: AppError) {
            throw e
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw ru.netology.network.error.UnknownError
        }
    }

    override suspend fun removeById(id: Long) {
        try {
            postdao.removeById(id)
            val response = apiService.removeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw ru.netology.network.error.UnknownError
        }
    }

    override suspend fun likeById(id: Long) {
        try {
            val response = apiService.getById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val post =
                response.body()?.toPost() ?: throw ApiError(response.code(), response.message())

            if (post.likedByMe) {
                val dislikedPost = post.copy(likedByMe = false)
                postdao.insert(PostEntity.fromDto(dislikedPost))
                val response2 = apiService.dislikeById(id)
                if (!response2.isSuccessful) {
                    throw ApiError(response2.code(), response2.message())
                }
            } else {
                val likedPost = post.copy(likedByMe = true)
                postdao.insert(PostEntity.fromDto(likedPost))
                val likeResponse = apiService.likeById(id)
                if (!likeResponse.isSuccessful) {
                    throw ApiError(likeResponse.code(), likeResponse.message())
                }
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw ru.netology.network.error.UnknownError
        }
    }

    override suspend fun userAuthentication(login: String, pass: String): AuthState {
        try {
            val authResponse = apiService.userAuthentication(login, pass)

            if (!authResponse.isSuccessful) {
                throw ApiError(authResponse.code(), authResponse.message())
            }

            val userById = apiService.getUserById(authResponse.body()?.id)

            if (!userById.isSuccessful) {
                throw ApiError(userById.code(), userById.message())
            }

            val id = authResponse.body()?.id ?: 0
            val token = authResponse.body()?.token
            val name = userById.body()?.name

            return AuthState(id, token, name)

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw ru.netology.network.error.UnknownError
        }
    }

    override suspend fun userRegistration(
        login: String,
        pass: String,
        name: String
    ): AuthState {
        try {
            val authResponse = apiService.userRegistration(login, pass, name)

            if (!authResponse.isSuccessful) {
                throw ApiError(authResponse.code(), authResponse.message())
            }

            val id = authResponse.body()?.id ?: 0
            val token = authResponse.body()?.token

            return AuthState(id, token, name)

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw ru.netology.network.error.UnknownError
        }
    }

    override suspend fun userRegistrationWithAvatar(
        login: String,
        pass: String,
        name: String,
        avatar: MediaRequest
    ): AuthState {
        try {
            val media = MultipartBody.Part.createFormData(
                "file", avatar.file.name, avatar.file.asRequestBody()
            )

            val authResponse = apiService.userRegistrationWithAvatar(login, pass, name, media)

            if (!authResponse.isSuccessful) {
                throw ApiError(authResponse.code(), authResponse.message())
            }

            val id = authResponse.body()?.id ?: 0
            val token = authResponse.body()?.token

            return AuthState(id, token, name)

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw ru.netology.network.error.UnknownError
        }
    }


    override suspend fun getUsers() {
        try {
            val response = apiService.getUsers()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val bodyResponse =
                response.body() ?: throw ApiError(response.code(), response.message())
            val users = bodyResponse.map {
                Users(
                    it.id,
                    it.name,
                    it.avatar
                )
            }
            userdao.insert(users.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw ru.netology.network.error.UnknownError
        }
    }
}