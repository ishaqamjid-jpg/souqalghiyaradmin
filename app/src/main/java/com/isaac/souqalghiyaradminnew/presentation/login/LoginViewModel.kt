package com.isaac.souqalghiyaradminnew.presentation.login

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.isaac.souqalghiyaradminnew.domain.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// حالة واجهة المستخدم
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

    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber: StateFlow<String> = _phoneNumber.asStateFlow()

    private val _rememberMe = MutableStateFlow(false)
    val rememberMe: StateFlow<Boolean> = _rememberMe.asStateFlow()

    fun onPhoneNumberChange(number: String) {
        _phoneNumber.value = number
    }

    fun onRememberMeChange(checked: Boolean) {
        _rememberMe.value = checked
    }

    fun login(onSuccess: (String, String, String) -> Unit) {
        val phone = _phoneNumber.value.trim()

        if (phone.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "يرجى إدخال رقم الهاتف")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // الاتصال بـ Repository للبحث عن الموظف برقم الهاتف
            val user = adminRepository.loginAdmin(phone)

            if (user != null && user.status == "active") {
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
                
                // الانتقال للداش بورد مع تمرير البيانات الهامة
                onSuccess(user.user_id, user.display_name, user.user_permissions)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "رقم الهاتف غير مسجل أو الحساب موقوف")
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
}
