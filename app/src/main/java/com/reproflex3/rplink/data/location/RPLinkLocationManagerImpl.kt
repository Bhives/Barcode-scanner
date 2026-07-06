package com.reproflex3.rplink.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.core.content.ContextCompat
import javax.inject.Inject

class RPLinkLocationManagerImpl @Inject constructor(private val context: Context) :
    RPLinkLocationManager {

    override fun getCoordinates(): Pair<Double, Double> {
        val locationManager =
            ContextCompat.getSystemService(context, LocationManager::class.java)
        val currentLocation: Location?
        var lat = 0.0
        var lon = 0.0

        if (locationManager != null) {
            val gpsLocation = getLocation(locationManager, LocationManager.GPS_PROVIDER)
            val networkLocation = getLocation(locationManager, LocationManager.NETWORK_PROVIDER)
            if (gpsLocation.accuracy > networkLocation.accuracy) {
                currentLocation = gpsLocation
                lat = currentLocation.latitude
                lon = currentLocation.longitude
            } else {
                currentLocation = networkLocation
                lat = currentLocation.latitude
                lon = currentLocation.longitude
            }
        }

        return Pair(lat, lon)
    }

    @SuppressLint("MissingPermission")
    private fun getLocation(locationManager: LocationManager, provider: String): Location {
        var currentLocation = Location(provider)
        val hasProvider = locationManager.isProviderEnabled(provider)

        val locationListener = LocationListener { location -> currentLocation = location }

        if (hasProvider) {
            locationManager.requestLocationUpdates(
                provider,
                MIN_TIME,
                MIN_DISTANCE,
                locationListener
            )
        }
        val lastKnownLocation = locationManager.getLastKnownLocation(provider)

        if (lastKnownLocation != null) {
            currentLocation = lastKnownLocation
        }
        return currentLocation
    }

    companion object {
        private const val MIN_TIME = 5000L
        private const val MIN_DISTANCE = 0f
    }
}