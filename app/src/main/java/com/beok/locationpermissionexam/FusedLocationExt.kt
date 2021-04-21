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
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@ExperimentalCoroutinesApi
@SuppressLint("MissingPermission")
suspend fun FusedLocationProviderClient.awaitLastLocation(): Location =
    suspendCancellableCoroutine { continuation ->
        lastLocation
            .addOnSuccessListener(continuation::resume)
            .addOnFailureListener(continuation::resumeWithException)
    }

@SuppressLint("MissingPermission")
@ExperimentalCoroutinesApi
fun FusedLocationProviderClient.locationFlow() = callbackFlow<Location> {
    val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            offer(result.lastLocation)
        }
    }

    requestLocationUpdates(
        LocationRequest
            .create()
            .apply {
                interval = TimeUnit.SECONDS.toMillis(2)
                fastestInterval = TimeUnit.SECONDS.toMillis(1)
                numUpdates = 1
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            },
        callback,
        Looper.getMainLooper()
    ).addOnFailureListener(::close)

    awaitClose {
        removeLocationUpdates(callback)
    }
}