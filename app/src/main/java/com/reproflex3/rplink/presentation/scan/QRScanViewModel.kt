package com.reproflex3.rplink.presentation.scan

import androidx.core.util.forEach
import androidx.core.util.isNotEmpty
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.reproflex3.rplink.data.location.RPLinkLocationManager
import com.reproflex3.rplink.data.preferences.PreferencesManagerImpl
import com.reproflex3.rplink.domain.repositoryinterface.RPLinkRepository
import com.reproflex3.rplink.presentation.model.ApiOutputResponse
import com.reproflex3.rplink.utils.PERMISSION_DENIAL_KEY
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QRScanViewModel @Inject constructor(
    private val preferencesManager: PreferencesManagerImpl,
    private val repository: RPLinkRepository,
    private val locationManager: RPLinkLocationManager
) : ViewModel() {

    private val _scanResultLink = MutableLiveData<String>()
    val scanResultLink: LiveData<String> = _scanResultLink

    private val _scanResult = MutableLiveData<String>()
    val scanResult: LiveData<String> = _scanResult

    private val _errorText = MutableLiveData<String>()
    val errorText: LiveData<String> = _errorText

    private var resultIsShowing = false

    fun setupProcessor(barcodeDetector: BarcodeDetector) {
        val coordinates = locationManager.getCoordinates()
        locationManager.getCoordinates()
        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {

            override fun release() {}

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                if (!resultIsShowing) {
                    resetValues()
                    val barcodes = detections.detectedItems
                    if (detections.detectedItems.isNotEmpty()) {
                        barcodes.forEach { _, value ->
                            if (!resultIsShowing) {
                                allowToScan(false)
                                if (value.url != null) {
                                    sendScanRequest(
                                        value.url.url,
                                        coordinates.first,
                                        coordinates.second,
                                        true
                                    )
                                } else {
                                    sendScanRequest(
                                        value.displayValue,
                                        coordinates.first,
                                        coordinates.second,
                                        false
                                    )
                                }
                            }
                        }
                    }
                }
            }
        })
    }

    private fun resetValues() {
        _scanResultLink.postValue("")
        _scanResult.postValue("")
        _errorText.postValue("")
    }

    private fun sendScanRequest(payload: String, lat: Double, lon: Double, isUrl: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val response = repository.getScanResult(payload, lat, lon, TYPE_QR)) {
                is ApiOutputResponse.Success -> {
                    if (isUrl) {
                        _scanResultLink.postValue(payload)
                    } else {
                        _scanResult.postValue(payload)
                    }
                }
                is ApiOutputResponse.Failure -> {
                    _errorText.postValue(response.exception.message.toString())
                }
            }
        }
    }

    fun updatePermissionPreferences(value: Int) {
        if (preferencesManager.getInt(PERMISSION_DENIAL_KEY) <= value) {
            preferencesManager.putInt(PERMISSION_DENIAL_KEY, value)
        }
    }

    fun allowToScan(resultIsShowing: Boolean) {
        this.resultIsShowing = !resultIsShowing
    }

    fun resetLink() {
        _scanResultLink.postValue("")
    }

    companion object {
        private const val TYPE_QR = "qr"
    }
}