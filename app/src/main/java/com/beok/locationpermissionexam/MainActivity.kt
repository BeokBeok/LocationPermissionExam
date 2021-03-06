package com.beok.locationpermissionexam

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.beok.locationpermissionexam.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var locationUtil: LocationUtil

    @Inject
    lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var binding: ActivityMainBinding
    private val viewModel by viewModels<MainViewModel>()

    private val requestLocation =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val isAllGranted = permissions.entries.all { it.value }
            if (!isAllGranted) {
                showFailToast()
                return@registerForActivityResult
            }
            requestGPS()
        }

    private val requestGPSSettings =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (!locationUtil.checkGPS()) {
                showFailToast()
                return@registerForActivityResult
            }
            recreate()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupBinding()
        startUpdatingLocation()
    }

    override fun onStart() {
        super.onStart()

        requestPermission()
        lifecycleScope.launchWhenStarted {
            runCatching {
                fusedLocationClient.awaitLastLocation()
            }.onSuccess(::showLocationToast)
        }
    }

    private fun setupBinding() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
    }

    private fun showFailToast() {
        Toast
            .makeText(this, "??????", Toast.LENGTH_SHORT)
            .show()
    }

    private fun requestGPS() {
        if (!locationUtil.checkGPS()) {
            requestGPSSettings.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
        recreate()
    }

    private fun requestPermission() {
        if (isNotValidLocationPermission()) {
            requestLocation.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            return
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

    private fun showLocationToast(location: Location) {
        Toast
            .makeText(
                this,
                "latitude is ${location.latitude} longitude is ${location.longitude}",
                Toast.LENGTH_SHORT
            )
            .show()
    }

    private fun startUpdatingLocation() {
        fusedLocationClient
            .locationFlow()
            .conflate()
            .catch { showFailToast() }
            .asLiveData()
            .observe(this) {
                showLocationToast(it)
            }
    }
}