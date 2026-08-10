package com.isaac.souqalghiyaradminnew.presentation.login

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.isaac.souqalghiyaradminnew.domain.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val userId: String = "",
    val adminName: String = "",
    val permissions: String = ""
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _rememberMe = MutableStateFlow(false)
    val rememberMe: StateFlow<Boolean> = _rememberMe.asStateFlow()

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
    }

    fun onPasswordChange(pass: String) {
        _password.value = pass
    }

    fun onRememberMeChange(checked: Boolean) {
        _rememberMe.value = checked
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    fun login(onSuccess: (String, String, String) -> Unit) {
        if (!isNetworkAvailable()) {
            _uiState.value = _uiState.value.copy(error = "لا يوجد اتصال بالإنترنت. يرجى التحقق من الشبكة.")
            return
        }

        val currentEmail = _email.value.trim()
        val pass = _password.value.trim()

        if (currentEmail.isEmpty() || pass.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "يرجى إدخال البريد الإلكتروني وكلمة المرور")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val user = adminRepository.loginAdmin(currentEmail, pass)

            if (user != null && user.status == "active") {
                try {
                    val token = FirebaseMessaging.getInstance().token.await()
                    adminRepository.updateFcmToken(user.user_id, token)
                    saveTokenLocally(token)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    userId = user.user_id,
                    adminName = user.display_name,
                    permissions = user.user_permissions
                )

                if (_rememberMe.value) {
                    saveSessionLocally(user.user_id, user.display_name, user.user_permissions)
                }

                onSuccess(user.user_id, user.display_name, user.user_permissions)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "البريد الإلكتروني أو كلمة المرور غير صحيحة، أو الحساب غير نشط")
            }
        }
    }

    private fun saveSessionLocally(userId: String, name: String, permissions: String) {
        val sharedPref = getApplication<Application>().getSharedPreferences("admin_prefs", Context.MODE_PRIVATE)
        sharedPref.edit().apply {
            putBoolean("is_logged_in", true)
            putString("admin_id", userId)
            putString("admin_name", name)
            putString("admin_permissions", permissions)
            apply()
        }
    }

    private fun saveTokenLocally(token: String) {
        val sharedPref = getApplication<Application>().getSharedPreferences("admin_prefs", Context.MODE_PRIVATE)
        sharedPref.edit().apply {
            putString("fcm_token", token)
            apply()
        }
    }
}
