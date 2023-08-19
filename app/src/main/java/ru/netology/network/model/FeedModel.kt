package ru.netology.network.model

import ru.netology.network.dto.Post


data class FeedModel(
    val posts: List<Post> = emptyList(),
    val empty: Boolean = false
)
