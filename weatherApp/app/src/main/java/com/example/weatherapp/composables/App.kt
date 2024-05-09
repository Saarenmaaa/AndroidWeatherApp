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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.weatherapp.R
import com.example.weatherapp.fetching.FetchWeather
import com.example.weatherapp.fetching.Hourly
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
        composable("dayView/{date}") { backStackEntry ->
            val date = backStackEntry.arguments?.getString("date") ?: ""
            DayView(navController = navController, date = date, hourly = fetchWeather.weatherData.value[0].hourly)
        }
    }
}

@Composable
fun DayView(navController: NavHostController, date: String, hourly: Hourly) {
    LazyColumn {
        item {
            Text(text = date)
        }
        items(hourly.time.size) { index ->
            if (hourly.time[index].substring(8, 10) == date.substring(0, 2)) {
                Row (modifier = Modifier.fillMaxSize()){
                    Text(text = hourly.time[index].substring(11))
                    Icon(
                        painter = painterResource(id = getWeatherDrawableResourceId(hourly.weather_code[index])),
                        contentDescription = "",
                        modifier = Modifier.size(30.dp)
                    )
                    Text(text = "${hourly.temperature_2m[index]}°C")
                    Text(text = "${hourly.wind_speed_10m[index]} m/s ")
                    Text(text = "${hourly.precipitation_probability[index]}")
                    Icon(
                        painter = painterResource(id = R.drawable.precipition),
                        contentDescription = "",
                        modifier = Modifier.size(70.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CurrentLocationDisplay(
    location: State<Location?>,
    fetchWeather: FetchWeather,
    reverseGeo: ReverseGeo,
    navController: NavHostController,)
    {
    var isLoading by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf("") }
    var latitude by remember { mutableDoubleStateOf(0.0) }
    var longitude by remember { mutableDoubleStateOf(0.0) }

    LaunchedEffect(location.value) {
        location.value?.let { loc ->
            try {
                latitude = loc.latitude
                longitude = loc.longitude
                if (name.isEmpty()) {
                    val cityName = reverseGeo.fetchGeoData(latitude, longitude)
                    fetchWeather.fetchWeatherData(latitude, longitude)
                    name = cityName
                }
                isLoading = false
            } catch (e: Exception) {
                println("Fetching failed")
            }
        }
    }

    Column (horizontalAlignment = Alignment.CenterHorizontally) {
        if (isLoading) {
            Text(text = "Finding Location")
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            if (name.isNotEmpty()) {
                println(fetchWeather.weatherData.collectAsState().value)
                val weather = fetchWeather.weatherData.collectAsState().value[0]
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Now", Modifier.padding(top = 10.dp), fontSize = 20.sp)
                    Row() {
                        Text(
                            text = name,
                            fontSize = 30.sp,
                            modifier = Modifier.padding(top = 11.dp),
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            painter = painterResource(id = getWeatherDrawableResourceId(weather.current.weather_code)),
                            contentDescription = "",
                            Modifier.size(70.dp)
                        )
                    }
                    Text(
                        text = weather.current.temperature_2m.toString() + "°C", fontSize = 70.sp,
                        fontFamily = FontFamily.Cursive, fontWeight = FontWeight.Bold
                    )
                }
                LazyColumn {
                    items(weather.daily.time.size) { index ->
                        val date = weather.daily.time[index].substring(8) + "." + weather.daily.time[index].substring(5, 7)
                        Row(Modifier.padding(10.dp)) {
                            Button(
                                onClick = { navController.navigate("dayView/${date}") },
                                shape = RoundedCornerShape(5),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = date, Modifier.padding(start = 0.dp), fontSize = 20.sp)
                                Column(Modifier.weight(0.6f), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = "Max ${weather.daily.temperature_2m_max[index]}°C", fontSize = 16.sp)
                                    Text(text = "Min ${weather.daily.temperature_2m_min[index]}°C", fontSize = 16.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10))
                                        .background(Color.LightGray)
                                ) {
                                    Icon(
                                        painter = painterResource(id = getWeatherDrawableResourceId(weather.daily.weather_code[index])),
                                        contentDescription = "",
                                        modifier = Modifier.matchParentSize()
                                    )
                                }
                                Text(text = weather.daily.precipitation_probability_max[index].toString(), Modifier.padding(start = 10.dp), fontSize = 20.sp)
                                Icon(
                                    painter = painterResource(id = R.drawable.precipition),
                                    contentDescription = "",
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                    }
                }

            } else {
                Text(text = "Location Found")
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
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