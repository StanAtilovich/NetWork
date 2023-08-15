package ru.netology.network.ui.activity.auth

data class LoginFormState(
    val isDataValid: Boolean = false,
    val isLoading: Boolean = false,
    val isError: Boolean = false
)
