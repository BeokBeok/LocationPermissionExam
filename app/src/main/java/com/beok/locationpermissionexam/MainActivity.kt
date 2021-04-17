package com.beok.locationpermissionexam

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var locationUtil: LocationUtil

    private val requestLocation =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val isAllGranted = permissions.entries.all { it.value }
            if (isAllGranted) {
                requestGPS()
            } else {
                Toast
                    .makeText(this, "실패", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    private val requestGPSSettings =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (!locationUtil.checkGPS()) {
                Toast
                    .makeText(this, "실패", Toast.LENGTH_SHORT)
                    .show()
                return@registerForActivityResult
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermission()
    }

    private fun requestGPS() {
        if (locationUtil.checkGPS()) {
            Toast
                .makeText(this, "성공", Toast.LENGTH_SHORT)
                .show()
            return
        }
        requestGPSSettings.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
    }

    private fun requestPermission() {
        if (isNotValidLocationPermission()) {
            requestLocation.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun isNotValidLocationPermission(): Boolean {
        val fineLocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val coarseLocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        return fineLocationPermission != PackageManager.PERMISSION_GRANTED &&
                coarseLocationPermission != PackageManager.PERMISSION_GRANTED
    }
}