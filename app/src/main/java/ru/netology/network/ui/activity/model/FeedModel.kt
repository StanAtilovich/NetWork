package ru.netology.network.ui.activity.model

import ru.netology.network.ui.activity.dto.Post

data class FeedModel(
    val posts: List<Post> = emptyList(),
    val empty: Boolean = false
)
