package com.example.weatherapp.fetching

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lab02.model.Product
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

@Serializable
data class Products(
    val products: List<Product>
)

interface ProductService {
    @GET("products")
    suspend fun getProducts(): Products
}

class ProductsViewModel : ViewModel() {
    // This is mutable observable list. If changes happens to this,
    // recomposition happens (ui refresh)
    //
    // This is private so we can encapsulate methods how to handle the list.
    private val _products : SnapshotStateList<Product> = mutableStateListOf<Product>()

    // Simple getter that exposes the mutable list as List to composables
    val products: List<Product> get() = _products

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://dummyjson.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val productService = retrofit.create(ProductService::class.java)

    init {
        // We will copy every product from List to a SnapShotStateList
        viewModelScope.launch {
            val productList = productService.getProducts()
            _products.addAll(productList.products)
        }
    }
    // We can add methods for dealing with the data
    fun getProductById(productId: Int?): Product? {
        // implement this!
        productId ?: return null
        return _products[productId-1]
    }
}