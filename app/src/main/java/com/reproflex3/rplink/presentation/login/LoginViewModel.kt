package com.reproflex3.rplink.presentation.login

import android.content.Intent
import android.net.Uri
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.reproflex3.rplink.R
import com.reproflex3.rplink.data.preferences.PreferencesManager
import com.reproflex3.rplink.data.preferences.PreferencesManagerImpl.Companion.REFRESH_TOKEN
import com.reproflex3.rplink.data.preferences.PreferencesManagerImpl.Companion.TOKEN
import com.reproflex3.rplink.domain.repositoryinterface.RPLinkRepository
import com.reproflex3.rplink.presentation.common.ResourceManager
import com.reproflex3.rplink.presentation.model.ApiOutputResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: RPLinkRepository,
    private val preferencesManager: PreferencesManager,
    private val resourceManager: ResourceManager
) : ViewModel() {

    private val _userLoggedIn = MutableLiveData<Boolean>()
    val userLoggedIn: LiveData<Boolean> = _userLoggedIn

    private val _emailErrorText = MutableLiveData<String>()
    val emailErrorText: LiveData<String> = _emailErrorText

    private val _passwordErrorText = MutableLiveData<String>()
    val passwordErrorText: LiveData<String> = _passwordErrorText

    private val _triggerForgotPasswordAction = MutableLiveData<Intent?>()
    val triggerForgotPasswordAction: LiveData<Intent?> = _triggerForgotPasswordAction

    init {
        checkUser()
    }

    fun login(email: String, password: String) {
        _emailErrorText.postValue("")
        _passwordErrorText.postValue("")
        _userLoggedIn.postValue(false)
        when {
            email.isBlank() ->
                _emailErrorText.value = resourceManager.getString(R.string.email_empty_error)
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                _emailErrorText.value = resourceManager.getString(R.string.incorrect_email_format)
            password.length < MIN_PASSWORD_LENGTH ->
                _passwordErrorText.value = resourceManager.getString(R.string.password_incorrect)
            else ->
                sendLoginRequest(email, password)
        }
    }

    private fun checkUser() {
        viewModelScope.launch(Dispatchers.IO) {
            when (repository.getCurrentUser()) {
                is ApiOutputResponse.Success -> {
                    _userLoggedIn.postValue(true)
                }
                is ApiOutputResponse.Failure -> {
                    _userLoggedIn.postValue(false)
                }
            }
        }
    }

    private fun sendLoginRequest(email: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val loginResponse = repository.login(email, password)) {
                is ApiOutputResponse.Success -> {
                    _userLoggedIn.postValue(true)
                    val data = loginResponse.data
                    preferencesManager.putString(TOKEN, data?.token.toString())
                    preferencesManager.putString(REFRESH_TOKEN, data?.refreshToken.toString())
                }
                is ApiOutputResponse.Failure -> {
                    _passwordErrorText.postValue(loginResponse.exception.message)
                }
            }
        }
    }

    fun goToForgotPassword() {
        _triggerForgotPasswordAction.value =
            Intent(Intent.ACTION_VIEW, Uri.parse(FORGOT_PASSWORD_LINK))
        _triggerForgotPasswordAction.value = null
    }

    companion object {
        private const val MIN_PASSWORD_LENGTH = 8
        private const val FORGOT_PASSWORD_LINK =
            "https://i-printed.reproflex3.com/auth/forgot-password"
    }
}