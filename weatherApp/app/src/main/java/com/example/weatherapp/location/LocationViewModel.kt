package com.example.weatherapp.location

import android.app.Application
import android.location.Location
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    private val locationRepository = LocationRepository(application)
    private val _location = MutableStateFlow<Location?>(null)
    val location : StateFlow<Location?> = _location.asStateFlow()

    /**
     * Function to startLocation updates
     *
     */
    @RequiresApi(Build.VERSION_CODES.S)
    fun startLocationUpdates() {
        locationRepository.startLocationUpdates { location ->
            _location.value = location
        }
    }
}