package ru.netology.network.ui.activity.dto

import ru.netology.network.ui.activity.enumeration.AttachmentType

data class Attachment(
    val url: String,
    val type : AttachmentType
)
