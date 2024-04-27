package com.example.weatherapp.composables

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.weatherapp.fetching.FetchViewModel
import com.example.weatherapp.location.LocationViewModel

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun App() {
    val viewModel: LocationViewModel = viewModel()
    val fetchModel: FetchViewModel = viewModel()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            // Check if all requested permissions have been granted
            val allPermissionsGranted = permissions.entries.all { it.value }
            if (allPermissionsGranted) {
                // Start location updates through the ViewModel if permissions are granted
                viewModel.startLocationUpdates()
            }
        }
    )
    LaunchedEffect(Unit) {
        permissionLauncher.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }
    Column {
        CurrentLocationDisplay(viewModel.location.collectAsState(), fetchModel)

    }
}

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun CurrentLocationDisplay(location: State<Location?>, fetchModel: FetchViewModel) {
    var isLoading by remember { mutableStateOf(true) }
    Text(text = location.value?.let { loc ->
            "Lat: ${loc.latitude}, Lon: ${loc.longitude}"
    } ?: "Location not available")
    location.value?.let { loc ->
        fetchModel.fetchWeatherData(loc.latitude, loc.longitude)
    }
    if (isLoading){
        CircularProgressIndicator()
    }

        LaunchedEffect(fetchModel.weatherData.value) {
            val weatherData = fetchModel.weatherData.value
            if (weatherData != null && weatherData.isNotEmpty()) {
                isLoading = false
            }
        }

        LazyColumn (modifier = Modifier.padding(top = 20.dp)){
            items(fetchModel.weatherData.value) {
                Text(text = "Temp: ${it.current.temperature_2m}")
                Text(text = "City: ${it.timezone}")
                it.hourly.time.forEachIndexed { i, time ->
                    val temperature = it.hourly.temperature_2m[i]
                    Text(text = "Time: $time, Temperature: $temperature")
                }
            }
        }
}




