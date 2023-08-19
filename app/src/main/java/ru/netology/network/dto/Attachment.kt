package ru.netology.network.dto

import ru.netology.network.enumeration.AttachmentType


data class Attachment(
    val url: String,
    val type: AttachmentType
)
