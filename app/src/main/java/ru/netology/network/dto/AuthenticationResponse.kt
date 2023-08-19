package ru.netology.network.dto

data class AuthenticationResponse(
    val id: Long = 0,
    val token: String? = null
)
