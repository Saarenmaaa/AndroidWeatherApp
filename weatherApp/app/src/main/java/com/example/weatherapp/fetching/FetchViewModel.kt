package com.example.weatherapp.fetching

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

@Serializable
data class WeatherData(
    val timezone: String,
    val hourly: Hourly,
)
@Serializable
data class Hourly(
    val time: List<String>,
    val temperature_2m: List<Double>
)

interface WeatherService {
    @GET("forecast")
    suspend fun getWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("hourly") hourly: String = "temperature_2m",
        @Query("timezone") timezone: String = "auto"
    ): WeatherData
}

class FetchViewModel : ViewModel() {
    private val _weatherData = MutableStateFlow<List<WeatherData>>(emptyList())
    val weatherData: StateFlow<List<WeatherData>> = _weatherData.asStateFlow()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/v1/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val weatherService = retrofit.create(WeatherService::class.java)
    fun fetchWeatherData(lat: Double, long: Double) {
        viewModelScope.launch {
            try {
                val weather = weatherService.getWeather(lat, long)
                _weatherData.value = listOf(weather)
                println(weather.hourly.temperature_2m)
            } catch (e: Exception) {
                e.printStackTrace()
                println("Fetching failed")
            }
        }
    }
    init {
        fetchWeatherData(1.11,1.1)
    }
}