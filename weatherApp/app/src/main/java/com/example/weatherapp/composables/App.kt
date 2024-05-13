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
import androidx.compose.runtime.collectAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.weatherapp.R
import com.example.weatherapp.fetching.FetchWeather
import com.example.weatherapp.location.LocationViewModel
import com.example.weatherapp.location.ReverseGeo

@SuppressLint("StateFlowValueCalledInComposition")
@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun App() {
    val viewModel: LocationViewModel = viewModel()
    val fetchWeather: FetchWeather = viewModel()
    val reverseGeo: ReverseGeo = viewModel()
    val navController = rememberNavController()

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

    NavHost(navController = navController, startDestination = "currentLocationScreen") {
        composable("currentLocationScreen") {
            CurrentLocationDisplay(location = viewModel.location.collectAsState(), fetchWeather, reverseGeo, navController)
        }
        composable("dayView/{date}/{idx}") { backStackEntry ->
            val date = backStackEntry.arguments?.getString("date") ?: ""
            val idx = backStackEntry.arguments?.getInt("idx") ?: 0
            DayView(date = date, weatherData = fetchWeather.weatherData.value[0], dayIdx = idx)
        }
    }
}

fun getWeatherDrawableResourceId(weatherCode: Int): Int {
    return when (weatherCode) {
        0 -> R.drawable.a0
        1 -> R.drawable.a1
        2 -> R.drawable.a2
        3 -> R.drawable.a3
        45 -> R.drawable.a45
        48 -> R.drawable.a48
        51 -> R.drawable.a51
        53 -> R.drawable.a53
        55 -> R.drawable.a55
        56, 57-> R.drawable.a56
        61 -> R.drawable.a61
        63 -> R.drawable.a63
        65 -> R.drawable.a65
        66 -> R.drawable.a66
        67 -> R.drawable.a67
        71 -> R.drawable.a71
        73 -> R.drawable.a73
        75 -> R.drawable.a75
        77 -> R.drawable.a77
        80 -> R.drawable.a80
        81 -> R.drawable.a81
        82 -> R.drawable.a82
        85 -> R.drawable.a85
        86 -> R.drawable.a86
        95 -> R.drawable.a95
        96 -> R.drawable.a96
        99 -> R.drawable.a99
        else -> R.drawable.a0
    }
}