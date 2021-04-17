package com.beok.locationpermissionexam

import android.location.LocationManager
import javax.inject.Inject

class LocationUtil @Inject constructor(private val locationManager: LocationManager) {

    fun checkGPS() = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
}