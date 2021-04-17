package com.beok.locationpermissionexam

import android.annotation.SuppressLint
import android.location.LocationManager
import javax.inject.Inject

class LocationUtil @Inject constructor(private val locationManager: LocationManager) {

    fun checkGPS() = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

    @SuppressLint("MissingPermission")
    fun coordinate(): Coordinate =
        (locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER))?.let {
            Coordinate(longitude = it.longitude, latitude = it.latitude)
        } ?: Coordinate()
}