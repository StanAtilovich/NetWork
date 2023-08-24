package ru.netology.network.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.network.auth.AuthState
import ru.netology.network.dto.Event
import ru.netology.network.dto.Job
import ru.netology.network.dto.MediaRequest
import ru.netology.network.dto.MediaResponse
import ru.netology.network.dto.Post
import ru.netology.network.dto.Users

interface PostRepository {
    val posts: Flow<List<Post>>
    val users: Flow<List<Users>>


    suspend fun getPosts()
    suspend fun getPostsByAuthor(userId: Long)
    suspend fun upload(upload: MediaRequest): MediaResponse
    suspend fun save(post: Post)
    suspend fun saveWithAttachment(post: Post, upload: MediaRequest)
    suspend fun removeById(id: Long)
    suspend fun likeById(id: Long)
    suspend fun userAuthentication(login: String, pass: String): AuthState
    suspend fun userRegistration(login: String, pass: String, name: String): AuthState
    suspend fun userRegistrationWithAvatar(
        login: String,
        pass: String,
        name: String,
        avatar: MediaRequest
    ): AuthState

    suspend fun getUsers()
}