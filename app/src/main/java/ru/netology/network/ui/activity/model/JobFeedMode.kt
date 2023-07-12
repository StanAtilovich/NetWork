package ru.netology.network.ui.activity.model

import ru.netology.network.ui.activity.dto.Job

data class JobFeedMode(
    val jobs: List<Job> = emptyList(),
    val empty: Boolean = false
)
