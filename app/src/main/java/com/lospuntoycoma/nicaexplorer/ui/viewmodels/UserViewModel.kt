package com.lospuntoycoma.nicaexplorer.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lospuntoycoma.nicaexplorer.data.FirebaseRepository
import com.lospuntoycoma.nicaexplorer.model.UserProfile
import com.lospuntoycoma.nicaexplorer.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para gestionar el estado del usuario y su rol en toda la app.
 */
class UserViewModel : ViewModel() {

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        val currentUser = FirebaseRepository.getCurrentUser()
        if (currentUser != null) {
            viewModelScope.launch {
                _isLoading.value = true
                val profile = FirebaseRepository.getUserProfile(currentUser.uid)
                _userProfile.value = profile
                _isLoading.value = false
            }
        } else {
            _userProfile.value = null
        }
    }

    fun isAdmin(): Boolean = _userProfile.value?.rol == UserRole.ADMIN
    fun isAuditor(): Boolean = _userProfile.value?.rol == UserRole.AUDITOR
}
