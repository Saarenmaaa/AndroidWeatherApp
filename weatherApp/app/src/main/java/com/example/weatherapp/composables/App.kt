package com.example.weatherapp.composables

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import android.Manifest
import android.location.Location
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
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
    LocationDisplay(viewModel.location.collectAsState(), fetchModel)
}

@Composable
fun LocationDisplay(location: State<Location?>, fetchModel: FetchViewModel) {
    Column {
        Text(text = location.value?.let { loc ->
            "Lat: ${loc.latitude}, Lon: ${loc.longitude}"
        } ?: "Location not available")
        Button(onClick = {
            location.value?.let { loc ->
                fetchModel.fetchWeatherData(loc.latitude, loc.longitude)

            }
        }) {
            Text("Fetch Weather Data")
        }
        LazyColumn (modifier = Modifier.padding(top = 20.dp)){
            items(fetchModel.weatherData.value) {
                Text(text = "${it.timezone} ${it.current.temperature_2m}")
                it.hourly.time.forEachIndexed { index, time ->
                    val temperature = it.hourly.temperature_2m[index]
                    Text(text = "Time: $time, Temperature: $temperature")
                }
            }
        }
    }
}




