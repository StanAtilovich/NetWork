package ru.netology.network.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.network.dto.Job

interface JobRepository {
    val jobs: Flow<List<Job>>

    suspend fun getJobs(userId: Long)
    suspend fun saveJob(job: Job)
    suspend fun removeJobById(id: Long)
}