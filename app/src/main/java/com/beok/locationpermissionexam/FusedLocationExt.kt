package com.beok.locationpermissionexam

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

@SuppressLint("MissingPermission")
@ExperimentalCoroutinesApi
fun FusedLocationProviderClient.locationFlow() = callbackFlow<Location> {
    val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            for (location in result.locations) {
                offer(location)
            }
        }
    }

    requestLocationUpdates(
        LocationRequest
            .create()
            .apply {
                interval = 3_000
                fastestInterval = 2_000
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            },
        callback,
        Looper.getMainLooper()
    ).addOnFailureListener(::close)

    awaitClose {
        removeLocationUpdates(callback)
    }
}