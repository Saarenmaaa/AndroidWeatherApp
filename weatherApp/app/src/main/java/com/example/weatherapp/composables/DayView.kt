package com.example.weatherapp.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherapp.R
import com.example.weatherapp.fetching.WeatherData
import kotlin.math.roundToInt

@Composable
fun DayView(date: String, weatherData: WeatherData, dayIdx: Int) {
    val hourly = weatherData.hourly
    val dateL = date.length
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center){
        Column (modifier = Modifier
            .weight(0.2f)
            .fillMaxWidth()
            .padding(top = 20.dp), horizontalAlignment = Alignment.CenterHorizontally){
            Text(text = date.substring(0, dateL-6), fontSize = 50.sp, fontWeight = FontWeight.ExtraLight)
            Text(text = date.substring( dateL-5,), fontSize = 30.sp, fontWeight = FontWeight.ExtraLight)
        }
        Column (modifier = Modifier
            .padding(10.dp)
            .weight(0.45f)
            .clip(RoundedCornerShape(6))
            .background(Color.DarkGray.copy(0.2f))
        )
        {
            Text(text = "HOURLY FORECAST", color = Color.White, modifier = Modifier.padding(start= 15.dp, top = 15.dp))
            LazyRow(modifier = Modifier.padding(10.dp)) {
                items(hourly.time.size) { index ->
                    val time = hourly.time[index]
                    if (time.substring(8, 10) == date.substring(date.length - 5, date.length - 3)) {
                        Column(modifier = Modifier
                            .padding(5.dp)
                            .shadow(2.dp, shape = RoundedCornerShape(5.dp))
                            .fillMaxHeight(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = time.substring(11),fontWeight = FontWeight.ExtraLight, fontSize = 25.sp, modifier = Modifier
                                .padding(top = 5.dp)
                                .weight(0.2f))
                            Icon(
                                painter = painterResource(id = getWeatherDrawableResourceId(hourly.weather_code[index])),
                                contentDescription = "",
                                modifier = Modifier
                                    .size(100.dp)
                                    .weight(0.4f).padding(5.dp),
                                tint = Color.White
                            )
                            Column (modifier = Modifier.weight(0.35f), horizontalAlignment = Alignment.CenterHorizontally){
                                Text(text = "${hourly.temperature_2m[index].roundToInt()}°", fontSize = 20.sp)
                                Text(text = " ${(hourly.wind_speed_10m[index] / 3.6).roundToInt()} m/s ", fontSize = 13.sp)
                                Row{
                                    Text(text = "${hourly.precipitation_probability[index]}")
                                    Icon(
                                        painter = painterResource(id = R.drawable.precipition),
                                        contentDescription = "",
                                        modifier = Modifier.size(25.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        Column (modifier = Modifier
            .padding(10.dp)
            .weight(0.35f)
            .clip(RoundedCornerShape(6))
            .background(Color.DarkGray.copy(0.2f))
            .fillMaxWidth()
        )
        {
            Text(text = "DAY INFO", color = Color.White, modifier = Modifier
                .padding(start = 15.dp, top = 15.dp)
                .weight(0.25f))
            Row (modifier = Modifier.weight(0.35f)){
                Column (modifier = Modifier
                    .weight(0.5f)
                    .padding(start = 15.dp)){
                    Text(text = "SUNRISE")
                    Text(text = weatherData.daily.sunrise[dayIdx].substring(weatherData.daily.sunrise[dayIdx].length-5),fontWeight = FontWeight.ExtraLight, color = Color.White)
                }
                Column (modifier = Modifier
                    .weight(0.5f)
                    .padding(start = 15.dp)){
                    Text(text = "SUNSET")
                    Text(text = weatherData.daily.sunset[dayIdx].substring(weatherData.daily.sunset[dayIdx].length-5), fontWeight = FontWeight.ExtraLight, color = Color.White)
                }
            }
            Row (modifier = Modifier.weight(0.35f)){
                Column (modifier = Modifier
                    .weight(0.5f)
                    .padding(start = 15.dp)){
                    Text(text = "RAIN SUM")
                    Text(text = weatherData.daily.rain_sum[dayIdx].toString() + "mm", fontWeight = FontWeight.ExtraLight, color = Color.White)

                }
                Column (modifier = Modifier
                    .weight(0.5f)
                    .padding(start = 15.dp)){
                    Text(text = "MAX UV INDEX")
                    Text(text = weatherData.daily.uv_index_max[dayIdx].roundToInt().toString(), fontWeight = FontWeight.ExtraLight, color = Color.White)
                }
            }
        }

    }
}
