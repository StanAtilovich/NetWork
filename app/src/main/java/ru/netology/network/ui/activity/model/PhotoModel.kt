package ru.netology.network.ui.activity.model

import android.net.Uri
import java.io.File

data class PhotoModel(
    var url: Uri? = null,
    val file: File? = null
)
