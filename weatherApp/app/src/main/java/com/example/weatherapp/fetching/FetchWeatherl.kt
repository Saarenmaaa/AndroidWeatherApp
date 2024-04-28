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
    val current: Current,
    val daily: Daily,
)
@Serializable
data class Daily(
    val time: List<String>,
    val temperature_2m_min: List<Double>,
    val temperature_2m_max: List<Double>,
    val weather_code: List<Int>,
    val precipitation_probability_max: List<Int>
)

@Serializable
data class Current(
    val temperature_2m: Double,
    val weather_code: Int
)

interface WeatherService {
    @GET("forecast")
    suspend fun getWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current:String = ("temperature_2m,weather_code"),
        @Query("daily") daily: String = ("weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max"),
        @Query("timezone") timezone: String = "auto"
    ): WeatherData
}

class FetchWeather : ViewModel() {
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
            } catch (e: Exception) {
                e.printStackTrace()
                println("Fetching failed")
            }
        }
    }
}