package ru.netology.network.ui.activity.model

import android.net.Uri
import java.io.File

data class PhotoModel(
    var uri: Uri? = null,
    val file: File? = null
)
