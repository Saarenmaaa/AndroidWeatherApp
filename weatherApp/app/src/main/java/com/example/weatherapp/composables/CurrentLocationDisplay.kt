package com.example.weatherapp.composables

import android.location.Location
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.weatherapp.R
import com.example.weatherapp.fetching.FetchWeather
import com.example.weatherapp.location.ReverseGeo
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun CurrentLocationDisplay(
    location: State<Location?>,
    fetchWeather: FetchWeather,
    reverseGeo: ReverseGeo,
    navController: NavHostController,
)
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
        .fillMaxSize()) {
        if (isLoading) {
            Column(modifier = Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
                Text(text = "Finding Location", fontSize = 25.sp, fontWeight = FontWeight.ExtraLight, modifier = Modifier.padding(10.dp))
                CircularProgressIndicator(color = Color.DarkGray)
            }
        } else {
            if (name.isNotEmpty()) {
                val weather = fetchWeather.weatherData.collectAsState().value[0]
                val dates = weather.daily.time[0].substring(8) + "." + weather.daily.time[0].substring(5, 7)
                val parsedDate = LocalDate.parse("$dates.${LocalDate.now().year}", DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                val date = parsedDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)+ " " + dates

                Column(modifier = Modifier
                    .fillMaxSize()
                    .weight(0.3f)
                    .padding(5.dp)
                    .shadow(1.dp, shape = RoundedCornerShape(3.dp))
                    .clickable { navController.navigate("dayView/${date}/${0}") },
                    horizontalAlignment = Alignment.Start
                ) {
                    Column(modifier = Modifier.weight(0.2f), horizontalAlignment = Alignment.Start)
                    {
                        Column(modifier = Modifier
                            .padding(top = 15.dp, start = 10.dp)
                            .fillMaxWidth(),)
                        {
                            Row {
                                Icon(
                                    painter = painterResource(id = R.drawable.location_pin_svgrepo_com),
                                    contentDescription = "", modifier = Modifier
                                        .size(15.dp)
                                        .padding(top = 5.dp),
                                )
                                Text(text = name, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = "Today " + weather.daily.time[0].substring(8) + "." + weather.daily.time[0].substring(5, 7),
                                fontSize = 10.sp, fontWeight = FontWeight.ExtraLight, modifier = Modifier.padding(start = 5.dp)
                            )
                        }
                    }

                    Row(modifier = Modifier.weight(0.6f))
                    {
                        Column (modifier = Modifier
                            .weight(0.4f)
                            .padding(5.dp)){
                            Icon(
                                painter = painterResource(id = getWeatherDrawableResourceId(weather.current.weather_code)),
                                contentDescription = "", modifier = Modifier
                                    .fillMaxSize(),
                                tint = Color.White
                            )
                        }
                        Column (modifier = Modifier
                            .weight(0.6f)
                            .padding(0.dp, 5.dp, 5.dp, 5.dp), horizontalAlignment = Alignment.CenterHorizontally, ){
                            Text(
                                text = "${weather.current.temperature_2m.roundToInt()}°",
                                fontSize = 100.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.6f)
                            )
                        }
                    }

                    Row(modifier = Modifier
                        .weight(0.2f)
                        .fillMaxWidth()
                        .padding(end = 20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
                        Icon(painter = painterResource(id = R.drawable.baseline_arrow_upward_24), contentDescription = "", tint = Color.Red)
                        Text(text = weather.daily.temperature_2m_max[0].roundToInt().toString() + "°")
                        Icon(painter = painterResource(id = R.drawable.baseline_arrow_downward_24), contentDescription = "", tint = Color.Blue)
                        Text(text = weather.daily.temperature_2m_min[0].roundToInt().toString() + "°  ")
                        Text(text = weather.daily.precipitation_probability_max[0].toString(), fontSize = 15.sp)
                        Icon(painter = painterResource(id = R.drawable.precipition), contentDescription = "", modifier = Modifier.size(25.dp), tint = Color.Blue)
                        Icon(painter = painterResource(id = R.drawable.wind_svgrepo_com), contentDescription = "", modifier = Modifier.size(15.dp), tint = Color.LightGray)
                        Text(text = (" " + (weather.current.wind_speed_10m / 3.6).roundToInt().toString() + " m/s "))
                    }
                }
                Column (modifier = Modifier.weight(0.5f))
                {
                    repeat(2) { rowIdx ->
                        Row(modifier = Modifier.weight(0.5f)) {
                            repeat(3) { colIdx->
                                val idx = rowIdx * 3 + colIdx + 1
                                val dater = weather.daily.time[idx].substring(8) + "." + weather.daily.time[idx].substring(5, 7)
                                val parsedDay = LocalDate.parse("$dater.${LocalDate.now().year}", DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                                val objectDate = parsedDay.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + dater
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(0.33f)
                                        .padding(3.dp)
                                        .clickable { navController.navigate("dayView/${objectDate}/${idx}") }
                                        .shadow(1.dp, shape = RoundedCornerShape(3.dp))
                                ) {
                                    Column(
                                        Modifier
                                            .fillMaxSize()
                                            .padding(10.dp)) {
                                        Box(modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(0.4f))
                                        {
                                            Icon(
                                                painter = painterResource(id = getWeatherDrawableResourceId(weather.daily.weather_code[idx])),
                                                contentDescription = "", modifier = Modifier.matchParentSize(),tint = Color.White
                                            )
                                        }
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(0.6f), horizontalAlignment = Alignment.CenterHorizontally)
                                        {
                                            Divider(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = objectDate.substring(0, 3).uppercase(),
                                                fontWeight = FontWeight.ExtraLight,
                                                fontSize = 25.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .weight(0.5f),
                                                color = Color.White
                                            )
                                            Row(modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(0.25f),
                                                horizontalArrangement = Arrangement.Center
                                            )
                                            {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.baseline_arrow_upward_24),
                                                    contentDescription = "", tint = Color.Red, modifier = Modifier.size(20.dp)
                                                )
                                                Text(text = "${weather.daily.temperature_2m_max[idx].roundToInt()}°", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                                Icon(
                                                    painter = painterResource(id = R.drawable.baseline_arrow_downward_24),
                                                    contentDescription = "", tint = Color.Blue, modifier = Modifier.size(20.dp)
                                                )
                                                Text(text = "${weather.daily.temperature_2m_min[idx].roundToInt()}°", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Row(modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(0.25f), horizontalArrangement = Arrangement.Center)
                                            {
                                                Text(text = weather.daily.precipitation_probability_max[idx].toString(), fontSize = 18.sp)
                                                Icon(painter = painterResource(id = R.drawable.precipition),
                                                    contentDescription = "", modifier = Modifier.size(25.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            else {
                Column(modifier = Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
                    Text(text = "Location Found", fontSize = 25.sp, fontWeight = FontWeight.ExtraLight, modifier = Modifier.padding(10.dp))
                    CircularProgressIndicator(color = Color.DarkGray)
                }
            }
        }
    }
}