package fpl.ph60001.techmart.network

import retrofit2.http.GET

// Data Models
data class HomeDataResponse(
    val banners: List<BannerDto>,
    val categories: List<CategoryDto>,
    val flashSale: List<ProductDto>
)

data class BannerDto(
    val id: Int,
    val imageUrl: String
)

data class CategoryDto(
    val id: Int,
    val name: String,
    val icon: String
)

data class ProductDto(
    val id: String,
    val name: String,
    val price: String,
    val discount: Int,
    val soldProgress: Float,
    val image: String
)

interface ApiService {
    @GET("api/home-data")
    suspend fun getHomeData(): HomeDataResponse
}
