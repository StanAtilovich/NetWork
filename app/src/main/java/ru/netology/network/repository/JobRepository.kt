package ru.netology.network.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.network.dto.Job

interface JobRepository {
    val jobs: Flow<List<Job>>

    suspend fun getJobs(userId: Long, currentUser: Long)
    suspend fun saveJob(userId: Long, job: Job)
    suspend fun removeJobById(id: Long)
}