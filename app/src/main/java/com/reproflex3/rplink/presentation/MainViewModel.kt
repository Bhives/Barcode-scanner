package com.reproflex3.rplink.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digimarc.dms.payload.Payload
import com.digimarc.dms.readers.BaseReader
import com.reproflex3.rplink.R
import com.reproflex3.rplink.data.location.RPLinkLocationManager
import com.reproflex3.rplink.data.preferences.PreferencesManager
import com.reproflex3.rplink.domain.repositoryinterface.RPLinkRepository
import com.reproflex3.rplink.presentation.common.ResourceManager
import com.reproflex3.rplink.presentation.model.ApiOutputResponse
import com.reproflex3.rplink.utils.PERMISSION_DENIAL_KEY
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: RPLinkRepository,
    private val preferencesManager: PreferencesManager,
    private val locationManager: RPLinkLocationManager,
    private val resourceManager: ResourceManager
) : ViewModel() {

    private val _email = MutableLiveData<String>()
    val email: LiveData<String> = _email

    private val _loggingOut = MutableLiveData<Boolean>()
    val loggingOut: LiveData<Boolean> = _loggingOut

    private val _logoutMessage = MutableLiveData<String>()
    val logoutMessage: LiveData<String> = _logoutMessage

    private val _shouldScan = MutableLiveData<Boolean>()
    val shouldScan: LiveData<Boolean> = _shouldScan

    private val _scanResult = MutableLiveData<String>()
    val scanResult: LiveData<String> = _scanResult

    private val _scanResultLink = MutableLiveData<String>()
    val scanResultLink: LiveData<String> = _scanResultLink

    private val _errorText = MutableLiveData<String>()
    val errorText: LiveData<String> = _errorText

    private var isBarcodeScanner = false

    init {
        _loggingOut.postValue(false)
        observeLogoutState()
        toggleScanInitialization(true)
    }

    fun updateUserInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            when (val response = repository.getCurrentUser()) {
                is ApiOutputResponse.Success -> {
                    _email.postValue(response.data?.email.toString())
                }
                is ApiOutputResponse.Failure -> {
                    _email.postValue(response.exception.message)
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.logout()
        }
    }

    private fun observeLogoutState() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.observeLogoutState().collect { shouldLogout ->
                if (shouldLogout.shouldLogout) {
                    _loggingOut.postValue(true)
                    _logoutMessage.postValue(shouldLogout.logoutMessage)
                } else {
                    _loggingOut.postValue(false)
                }
            }
        }
    }

    fun toggleScanInitialization(init: Boolean) {
        _shouldScan.postValue(init)
        if (init) {
            resetValues()
        }
    }

    fun processScanResult(payload: Payload?) {
        resetValues()
        val coordinates = locationManager.getCoordinates()
        viewModelScope.launch(Dispatchers.Default) {
            when (payload?.symbology) {
                BaseReader.ImageSymbology.Image_Digimarc -> {
                    val scanRepresentation =
                        payload.getRepresentation(Payload.BasicRepresentation.PrintID)
                    if (scanRepresentation != null) {
                        val payloadOutput =
                            scanRepresentation.toString().toInt(HEX_FORMAT)
                                .toString()
                        if (!isBarcodeScanner) {
                            toggleScanInitialization(false)
                            sendScanRequest(
                                payloadOutput,
                                coordinates.first,
                                coordinates.second,
                                TYPE_DIGIMARC
                            )
                        }
                    } else {
                        _errorText.postValue(resourceManager.getString(R.string.no_match))
                    }
                }
                BaseReader.ImageSymbology.Image_1D_Code128 -> {
                    val scanRepresentation =
                        payload.getRepresentation(Payload.BasicRepresentation.Code_128)
                    if (scanRepresentation != 0) {
                        val payloadOutput = scanRepresentation.toString()
                        toggleScanInitialization(false)
                        sendScanRequest(
                            payloadOutput,
                            coordinates.first,
                            coordinates.second,
                            TYPE_BARCODE
                        )
                    }
                }
            }
        }
    }

    private suspend fun sendScanRequest(payload: String, lat: Double, lon: Double, type: String) {
        val response = withContext(viewModelScope.coroutineContext + Dispatchers.IO) {
            repository.getScanResult(payload, lat, lon, type)
        }
        when (response) {
            is ApiOutputResponse.Success -> {
                when (type) {
                    TYPE_DIGIMARC -> {
                        _scanResultLink.postValue(response.data?.url.toString())
                    }
                    TYPE_BARCODE -> {
                        _scanResult.postValue(payload)
                    }
                }
            }
            is ApiOutputResponse.Failure -> {
                _errorText.postValue(response.exception.message)
            }
        }
    }

    fun resetLink() {
        _scanResultLink.postValue("")
        toggleScanInitialization(true)
    }

    fun allowToScan(resultIsShowing: Boolean) {
        _shouldScan.postValue(resultIsShowing)
    }

    fun displayError(error: String) {
        _errorText.postValue(error)
    }

    fun resetValues() {
        _scanResultLink.postValue("")
        _scanResult.postValue("")
        _errorText.postValue("")
    }

    fun updatePermissionPreferences(value: Int) {
        if (preferencesManager.getInt(PERMISSION_DENIAL_KEY) <= value) {
            preferencesManager.putInt(PERMISSION_DENIAL_KEY, value)
        }
    }

    fun toggleScannerType(isBarcode: Boolean) {
        isBarcodeScanner = isBarcode
    }

    companion object {
        private const val HEX_FORMAT = 16
        private const val TYPE_DIGIMARC = "digimarc"
        private const val TYPE_BARCODE = "barcode"
    }
}