package ru.netology.network.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import ru.netology.network.api.ApiService
import ru.netology.network.auth.AppAuth
import ru.netology.network.dao.JobDao
import ru.netology.network.dto.Job
import ru.netology.network.dto.JobRequest
import ru.netology.network.error.ApiError
import ru.netology.network.error.NetworkError
import ru.netology.nework.entity.JobEntity
import ru.netology.nework.entity.toDto
import ru.netology.nework.entity.toEntity
import java.io.IOException

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
 class JobRepositoryImpl @Inject constructor(
    private val jobdao: JobDao,
    private val apiService: ApiService,
    private val appAuth: AppAuth,

) : JobRepository{
    override val jobs = jobdao.getJobs()
        .map(List<JobEntity>::toDto)
        .flowOn(Dispatchers.Default)

    override suspend fun getJobs(userId: Long) {
        try {
            val response = apiService.getJobsByUserId(userId)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val bodyResponse =
                response.body() ?: throw ApiError(response.code(), response.message())
            val currentUser = appAuth.authStateFlow.value.id
            val job = bodyResponse.map {
                Job(
                    userId,
                    userId == currentUser,
                    it.id,
                    it.name,
                    it.position,
                    it.start,
                    it.finish,
                    it.link,
                )
            }
            jobdao.insert(job.toEntity())

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw ru.netology.network.error.UnknownError
        }
    }

    override suspend fun saveJob(job: Job) {
        try {
            val jobRequest = JobRequest(
                job.id,
                job.name,
                job.position,
                job.start,
                job.finish,
                job.link
            )
            val response = apiService.saveJob(jobRequest)

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val currentUser = appAuth.authStateFlow.value.id
            val bodyResponse =
                response.body() ?: throw ApiError(response.code(), response.message())
            val job2save = Job(
                currentUser,
                true,
                bodyResponse.id,
                bodyResponse.name,
                bodyResponse.position,
                bodyResponse.start,
                bodyResponse.finish,
                bodyResponse.link,
            )
            jobdao.insert(JobEntity.fromDto(job2save))

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw ru.netology.network.error.UnknownError
        }

    }

    override suspend fun removeJobById(id: Long) {
        try {
            jobdao.removeById(id)
            val response = apiService.removeJobById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw ru.netology.network.error.UnknownError
        }
    }
}