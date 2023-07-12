package ru.netology.network.ui.activity.error

import android.database.SQLException
import java.io.IOException

class ApiError(val status: Int, code: String) : AppError(code)
object NetworkError : AppError("Ошибка сети")
object DbError : AppError("Ошибка базы данных")
object UnknownError : AppError("Неизвестная ошибка")


sealed class AppError(var code: String) : RuntimeException() {
    companion object {
        fun from(e: Throwable): AppError = when (e) {
            is AppError -> e
            is SQLException -> DbError
            is IOException -> NetworkError
            else -> UnknownError
        }
    }
}