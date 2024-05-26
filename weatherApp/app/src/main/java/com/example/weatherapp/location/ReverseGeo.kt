package com.example.weatherapp.location

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

    @Serializable data class GeoData(
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

    // GeoService to get reverse GeoCoding from latitude and longitude
    interface GeoService {
        @GET("json")
        suspend fun getGeo(
            @Query("latlng") latlng: String,
            @Query("key") current: String = "Apikeyhere"
        ): GeoData
    }

    /**
     * ViewModel for Google reverseGeocoding API
     * Takes lat, long values and returns name of the district
     */
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
                    val getGeo = geoService.getGeo(latlng).results[3].address_components[3].long_name
                    geo.value = getGeo
                } catch (e: Exception) {
                    e.printStackTrace()
                    println("Fetching failed")
                }
            }
            return geo.value
        }
    }

