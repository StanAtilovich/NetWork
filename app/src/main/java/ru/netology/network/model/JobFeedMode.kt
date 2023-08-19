package ru.netology.network.model

import ru.netology.network.dto.Job


data class JobFeedMode(
    val jobs: List<Job> = emptyList(),
    val empty: Boolean = false
)
