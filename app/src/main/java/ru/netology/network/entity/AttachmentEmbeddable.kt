package ru.netology.network.entity

import ru.netology.network.dto.Attachment
import ru.netology.network.enumeration.AttachmentType


data class AttachmentEmbeddable(
    var url: String,
    var attachmentType: AttachmentType,
) {
    fun toDto() = Attachment(url, attachmentType)

    companion object {
        fun fromDto(dto: Attachment?) = dto?.let {
            AttachmentEmbeddable(it.url, it.type)
        }
    }
}
