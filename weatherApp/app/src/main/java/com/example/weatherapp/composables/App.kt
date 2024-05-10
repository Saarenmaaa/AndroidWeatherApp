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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt


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
            DayView(date = date, hourly = fetchWeather.weatherData.value[0].hourly)
        }
    }
}

@Composable
fun DayView(date: String, hourly: Hourly) {
    val dateL = date.length
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center){
        Column (modifier = Modifier.weight(0.2f), horizontalAlignment = Alignment.CenterHorizontally){
            Text(text = date.substring(0, dateL-6), fontSize = 50.sp, fontWeight = FontWeight.Bold)
            Text(text = date.substring( dateL-5,), fontSize = 30.sp, fontWeight = FontWeight.ExtraLight)
        }
        LazyColumn (modifier = Modifier.weight(0.80f)){
            item {
            }
            items(hourly.time.size) { index ->
                if (hourly.time[index].substring(8, 10) == date.substring( dateL-5,dateL-3)) {
                    Row (modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.LightGray)
                        .padding(5.dp), verticalAlignment = Alignment.CenterVertically){
                        Text(text = hourly.time[index].substring(11))
                        Icon(
                            painter = painterResource(id = getWeatherDrawableResourceId(hourly.weather_code[index])),
                            contentDescription = "", modifier = Modifier.size(30.dp)
                        )
                        Text(text = "${hourly.temperature_2m[index]}°C")
                        Text(text = "${hourly.wind_speed_10m[index]} m/s ")
                        Text(text = "${hourly.precipitation_probability[index]}")
                        Icon(
                            painter = painterResource(id = R.drawable.precipition),
                            contentDescription = "", modifier = Modifier.size(70.dp)
                        )
                    }
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

    Column (modifier = Modifier
        .fillMaxSize()
        .background(Color.LightGray)) {
        if (isLoading) {
            Column(modifier = Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
                Text(text = "Finding Location", fontSize = 25.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(10.dp))
                CircularProgressIndicator()
            }
        } else {
            if (name.isNotEmpty()) {
                val weather = fetchWeather.weatherData.collectAsState().value[0]
                val dates = weather.daily.time[0].substring(8) + "." + weather.daily.time[0].substring(5, 7)
                val parsedDate = LocalDate.parse("$dates.${LocalDate.now().year}", DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                val date = parsedDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)+ " " + dates

                Button(
                        onClick = { navController.navigate("dayView/${date}") },
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(0.3f)
                            .padding(20.dp),
                        shape = RoundedCornerShape(10),
                        colors = ButtonDefaults.buttonColors(
                            Color.Blue.copy(alpha = 0.2f),
                            contentColor = Color.White
                        ),
                    ) {
                        Column (Modifier.weight(0.5f)){
                            Column (Modifier.weight(0.2f)){
                                Row {
                                    Icon(
                                        painter = painterResource(id = R.drawable.location_pin_svgrepo_com),
                                        contentDescription = "",
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(text = name, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(text = "Today " + weather.daily.time[0].substring(8) + "." + weather.daily.time[0].substring(5, 7), fontSize = 15.sp, fontWeight = FontWeight.ExtraLight)
                            }
                            Icon(
                                painter = painterResource(id = getWeatherDrawableResourceId(weather.current.weather_code)),
                                contentDescription = "",
                                modifier = Modifier.size(110.dp).weight(0.8f)
                            )
                        }
                        Column (Modifier.weight(0.5f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center){
                            Text(
                                text = "${weather.current.temperature_2m.roundToInt()}°",
                                fontSize = 100.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }

                Column (modifier = Modifier
                    .weight(0.5f)
                    )
                {
                    Row(modifier = Modifier.weight(0.5f)) {
                        Button(
                            colors = ButtonDefaults.buttonColors(
                                Color.Red.copy(alpha = 0.5f),
                                contentColor = Color.White
                            ),
                            onClick = { navController.navigate("dayView/${date}") },
                            shape = RoundedCornerShape(5),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(0.33f)
                                .padding(3.dp)
                        ) {
                            val dates = weather.daily.time[1].substring(8) + "." + weather.daily.time[1].substring(5, 7)
                            val parsedDate = LocalDate.parse("$dates.${LocalDate.now().year}", DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                            val date = parsedDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)+ " " + dates
                            Column (Modifier.fillMaxSize()){
                                Box(modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(0.4f))
                                {
                                    Icon(painter = painterResource(id = getWeatherDrawableResourceId(weather.daily.weather_code[1])),
                                        contentDescription = "",
                                        modifier = Modifier.matchParentSize())
                                }
                                Column (modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(0.6f), horizontalAlignment = Alignment.CenterHorizontally){
                                    Divider(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = date.substring(0, 3).uppercase(),
                                        fontSize = 25.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(0.5f)
                                    )
                                    Row (modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(0.25f)){
                                        Text(text = "${weather.daily.temperature_2m_max[1].roundToInt()}° |", fontSize = 15.sp)
                                        Text(text = " ${weather.daily.temperature_2m_min[1].roundToInt()}°", fontSize = 15.sp)
                                    }
                                    Row (modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(0.25f)){
                                        Text(text = weather.daily.precipitation_probability_max[1].toString(), fontSize = 20.sp)
                                        Icon(painter = painterResource(id = R.drawable.precipition), contentDescription = "", modifier = Modifier.size(30.dp))
                                    }
                                }
                                Divider(modifier = Modifier.height(2.dp))
                            }
                        }
                        Button(
                            colors = ButtonDefaults.buttonColors(
                                Color.Red.copy(alpha = 0.5f),
                                contentColor = Color.White
                            ),
                            onClick = { navController.navigate("dayView/${date}") },
                            shape = RoundedCornerShape(5),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(0.33f)
                                .padding(3.dp)
                        ) {
                            Column (Modifier.fillMaxSize()){
                                Box(modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(0.4f))
                                {
                                    Icon(painter = painterResource(id = getWeatherDrawableResourceId(weather.daily.weather_code[1])),
                                        contentDescription = "",
                                        modifier = Modifier.matchParentSize())
                                }
                                Column (modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(0.6f), horizontalAlignment = Alignment.CenterHorizontally){
                                    Divider(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = date.substring(0, 3).uppercase(),
                                        fontSize = 25.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(0.5f)
                                    )
                                    Row (modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(0.25f)){
                                        Text(text = "${weather.daily.temperature_2m_max[1].roundToInt()}° |", fontSize = 15.sp)
                                        Text(text = " ${weather.daily.temperature_2m_min[1].roundToInt()}°", fontSize = 15.sp)
                                    }
                                    Row (modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(0.25f)){
                                        Text(text = weather.daily.precipitation_probability_max[1].toString(), fontSize = 20.sp)
                                        Icon(painter = painterResource(id = R.drawable.precipition), contentDescription = "", modifier = Modifier.size(30.dp))
                                    }
                                }
                                Divider(modifier = Modifier.height(2.dp))
                            }

                        }
                        Button(
                            colors = ButtonDefaults.buttonColors(
                                Color.Red.copy(alpha = 0.5f),
                                contentColor = Color.White
                            ),
                            onClick = { navController.navigate("dayView/${date}") },
                            shape = RoundedCornerShape(5),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(0.33f)
                                .padding(3.dp)
                        ) {
                            Column (Modifier.fillMaxSize()){
                                Box(modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(0.4f))
                                {
                                    Icon(painter = painterResource(id = getWeatherDrawableResourceId(weather.daily.weather_code[1])),
                                        contentDescription = "",
                                        modifier = Modifier.matchParentSize())
                                }
                                Column (modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(0.6f), horizontalAlignment = Alignment.CenterHorizontally){
                                    Divider(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = date.substring(0, date.length-5).uppercase(),
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(0.5f)
                                    )
                                    Row (modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(0.25f)){
                                        Text(text = "${weather.daily.temperature_2m_max[1].roundToInt()}° |", fontSize = 15.sp)
                                        Text(text = " ${weather.daily.temperature_2m_min[1].roundToInt()}°", fontSize = 15.sp)
                                    }
                                    Row (modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(0.25f)){
                                        Text(text = weather.daily.precipitation_probability_max[1].toString(), fontSize = 20.sp)
                                        Icon(painter = painterResource(id = R.drawable.precipition), contentDescription = "", modifier = Modifier.size(30.dp))
                                    }
                                }
                                Divider(modifier = Modifier.height(2.dp))
                            }

                        }
                        }
                    Row(modifier = Modifier.weight(0.5f)) {
                        Button(
                            colors = ButtonDefaults.buttonColors(
                                Color.Red.copy(alpha = 0.5f),
                                contentColor = Color.White
                            ),
                            onClick = { navController.navigate("dayView/${date}") },
                            shape = RoundedCornerShape(5),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(0.33f)
                                .padding(3.dp)
                        ) {
                            Column (Modifier.fillMaxSize()){
                                Box(modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(0.4f))
                                {
                                    Icon(painter = painterResource(id = getWeatherDrawableResourceId(weather.daily.weather_code[1])),
                                        contentDescription = "",
                                        modifier = Modifier.matchParentSize())
                                }
                                Column (modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(0.6f), horizontalAlignment = Alignment.CenterHorizontally){
                                    Divider(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = date.substring(0, 3).uppercase(),
                                        fontSize = 25.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(0.5f)
                                    )
                                    Row (modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(0.25f)){
                                        Text(text = "${weather.daily.temperature_2m_max[1].roundToInt()}° |", fontSize = 15.sp)
                                        Text(text = " ${weather.daily.temperature_2m_min[1].roundToInt()}°", fontSize = 15.sp)
                                    }
                                    Row (modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(0.25f)){
                                        Text(text = weather.daily.precipitation_probability_max[1].toString(), fontSize = 20.sp)
                                        Icon(painter = painterResource(id = R.drawable.precipition), contentDescription = "", modifier = Modifier.size(30.dp))
                                    }
                                }
                                Divider(modifier = Modifier.height(2.dp))
                            }
                        }
                        Button(
                            colors = ButtonDefaults.buttonColors(
                                Color.Red.copy(alpha = 0.5f),
                                contentColor = Color.White
                            ),
                            onClick = { navController.navigate("dayView/${date}") },
                            shape = RoundedCornerShape(5),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(0.33f)
                                .padding(3.dp)
                        ) {
                            Column (Modifier.fillMaxSize()){
                                Box(modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(0.4f))
                                {
                                    Icon(painter = painterResource(id = getWeatherDrawableResourceId(weather.daily.weather_code[1])),
                                        contentDescription = "",
                                        modifier = Modifier.matchParentSize())
                                }
                                Column (modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(0.6f), horizontalAlignment = Alignment.CenterHorizontally){
                                    Divider(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = date.substring(0, 3).uppercase(),
                                        fontSize = 25.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(0.5f)
                                    )
                                    Row (modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(0.25f)){
                                        Text(text = "${weather.daily.temperature_2m_max[1].roundToInt()}° |", fontSize = 15.sp)
                                        Text(text = " ${weather.daily.temperature_2m_min[1].roundToInt()}°", fontSize = 15.sp)
                                    }
                                    Row (modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(0.25f)){
                                        Text(text = weather.daily.precipitation_probability_max[1].toString(), fontSize = 20.sp)
                                        Icon(painter = painterResource(id = R.drawable.precipition), contentDescription = "", modifier = Modifier.size(30.dp))
                                    }
                                }
                                Divider(modifier = Modifier.height(2.dp))
                            }

                        }
                        Button(
                            colors = ButtonDefaults.buttonColors(
                                Color.Red.copy(alpha = 0.5f),
                                contentColor = Color.White
                            ),
                            onClick = { navController.navigate("dayView/${date}") },
                            shape = RoundedCornerShape(5),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(0.33f)
                                .padding(3.dp)
                        ) {
                            Column (Modifier.fillMaxSize()){
                                Box(modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(0.4f))
                                {
                                    Icon(painter = painterResource(id = getWeatherDrawableResourceId(weather.daily.weather_code[1])),
                                        contentDescription = "",
                                        modifier = Modifier.matchParentSize())
                                }
                                Column (modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(0.6f), horizontalAlignment = Alignment.CenterHorizontally){
                                    Divider(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = date.substring(0, 3).uppercase(),
                                        fontSize = 25.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(0.5f)
                                    )
                                    Row (modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(0.25f)){
                                        Text(text = "${weather.daily.temperature_2m_max[1].roundToInt()}° |", fontSize = 15.sp)
                                        Text(text = " ${weather.daily.temperature_2m_min[1].roundToInt()}°", fontSize = 15.sp)
                                    }
                                    Row (modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(0.25f)){
                                        Text(text = weather.daily.precipitation_probability_max[1].toString(), fontSize = 20.sp)
                                        Icon(painter = painterResource(id = R.drawable.precipition), contentDescription = "", modifier = Modifier.size(30.dp))
                                    }
                                }
                                Divider(modifier = Modifier.height(2.dp))
                            }

                        }
                    }

                }
                }
            else {
                Column(modifier = Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
                    Text(text = "Location Found", fontSize = 25.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(10.dp))
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