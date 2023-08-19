package ru.netology.network.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.netology.network.auth.AppAuth
import ru.netology.network.auth.AuthState
import ru.netology.network.auth.LoginFormState
import ru.netology.network.dto.MediaRequest
import ru.netology.network.model.PhotoModel
import ru.netology.network.repository.PostRepository
import java.io.File
import javax.inject.Inject


@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: AppAuth,
    private val repository: PostRepository
) : ViewModel() {
    val data: LiveData<AuthState> = auth
        .authStateFlow
        .asLiveData(Dispatchers.Default)

    private val _photoAvatar = MutableLiveData(noPhotoAvatar)
    val photoAvatar: LiveData<PhotoModel>
        get() = _photoAvatar

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    fun loginDataChanged(userName: String, password: String) {
        _loginForm.value =
            LoginFormState(isDataValid = isUserNameValid(userName) && isPasswordValid(password))
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.isNotEmpty()
    }

    private fun isUserNameValid(username: String): Boolean {
        return username.isNotEmpty()
    }

    fun userAuthentication(login: String, password: String) = viewModelScope.launch {
        try {
            _loginForm.value = LoginFormState(isLoading = true)
            val acount = repository.userAuthentication(login, password)
            auth.setAuth(acount.id, acount.token, acount.name)
            _loginForm.value = LoginFormState(isDataValid = true)
        } catch (e: Exception) {
            _loginForm.value = LoginFormState(isError = true, isDataValid = true)
        }
    }

    fun userRegistration(login: String, password: String, name: String) = viewModelScope.launch {
        try {
            _loginForm.value = LoginFormState(isLoading = true)
            val acount = when (_photoAvatar.value) {
                noPhotoAvatar -> repository.userRegistration(login, password, name)
                else -> _photoAvatar.value?.file?.let { file ->
                    repository.userRegistrationWithAvatar(login, password, name, MediaRequest(file))
                }
            }
            if (acount != null) {
                auth.setAuth(acount.id, acount.token, acount.name)
            }
            _loginForm.value = LoginFormState(isDataValid = true)
        } catch (e: Exception) {
            _loginForm.value = LoginFormState(isError = true, isDataValid = true)
        }
    }

    fun changeAvatar(uri: Uri?, file: File?) {
        _photoAvatar.value = PhotoModel(uri, file)
    }
}

private val noPhotoAvatar = PhotoModel()