package com.reproflex3.rplink.presentation.scan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.reproflex3.rplink.data.preferences.PreferencesManager
import com.reproflex3.rplink.utils.PERMISSION_DENIAL_KEY
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RequestPermissionViewModel @Inject constructor(private val preferencesManager: PreferencesManager) :
    ViewModel() {

    private val _permissionPermanentlyDenied = MutableLiveData<Boolean>()
    val permissionPermanentlyDenied: LiveData<Boolean> = _permissionPermanentlyDenied

    init {
        updatePermissionStatus()
    }

    fun updatePermissionPreferences(value: Int) {
        preferencesManager.putInt(PERMISSION_DENIAL_KEY, value)
        updatePermissionStatus()
    }

    private fun updatePermissionStatus() {
        _permissionPermanentlyDenied.value =
            preferencesManager.getInt(PERMISSION_DENIAL_KEY) >= PERMANENT_PERMISSION_DENIAL_CONDITION
    }

    companion object {
        private const val PERMANENT_PERMISSION_DENIAL_CONDITION = 2
    }
}