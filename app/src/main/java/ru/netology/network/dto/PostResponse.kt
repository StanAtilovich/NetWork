package ru.netology.network.dto

data class PostResponse(
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String? = null,
    val authorJob: String? = null,
    val content: String,
    val published: String,
    val coords: Coordinates? = null,
    val link: String? = null,
    val likeOwnerIds: List<Long>? = emptyList(),
    val mentionIds: List<Long>? = emptyList(),
    val mentionedMe: Boolean = false,
    val likedByMe: Boolean = false,
    val attachment: Attachment? = null,
    val ownedByMe: Boolean = false,
    val users: Map<String, UserPreview>?,
)

fun PostResponse.toPost(): Post{
    return Post(
       id,
        authorId,
        author,
        authorAvatar,
        authorJob,
        content,
        published,
        coords,
        link,
        likeOwnerIds,
        mentionIds,
        mentionIds?.map { id ->
            users?.get(id.toString())!!.name
        },
        mentionedMe,
        likedByMe,
        attachment,
        ownedByMe
    )

}



