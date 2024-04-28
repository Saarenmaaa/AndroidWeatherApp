package com.example.weatherapp.location

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.fetching.WeatherData
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
    data class GeoData(
        val results: List<Result>,
    )
    @Serializable
    data class Result(
        val address_components: List<address_component>,
    )
    @Serializable
    data class address_component(
        val long_name: String,
    )


    interface GeoService {
        @GET("json")
        suspend fun getGeo(
            @Query("latlng") latlng: String,
            @Query("key") current: String = "AIzaSyBnopuYrOmmUApUZNRUUtXGxquqia-RR9Q"
        ): GeoData
    }

    class ReverseGeo : ViewModel() {
        private var geo: MutableState<String> = mutableStateOf("")

        private val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/maps/api/geocode/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        private val geoService = retrofit.create(GeoService::class.java)
        fun fetchGeoData(lat: Double, long: Double): String {
            viewModelScope.launch {
                try {
                    val latlng = "${lat},${long}"
                    val getGeo = geoService.getGeo(latlng).results[3].address_components[2].long_name
                    geo.value = getGeo
                    println(geo.value)
                } catch (e: Exception) {
                    e.printStackTrace()
                    println("Fetching failed")
                }
            }
            return geo.value
        }
    }
