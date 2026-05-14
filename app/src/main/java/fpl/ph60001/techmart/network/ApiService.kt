package fpl.ph60001.techmart.network

import retrofit2.http.GET
import retrofit2.http.Path

// Data Models
data class HomeDataResponse(
    val banners: List<BannerDto>,
    val categories: List<CategoryDto>,
    val flashSale: List<ProductDto>,
    val allProducts: List<SimpleProductDto>
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

data class SimpleProductDto(
    val id: String,
    val name: String,
    val price: String,
    val image: String,
    val categoryId: Int
)

data class ProductDetailDto(
    val id: String,
    val name: String,
    val price: String,
    val image: String,
    val description: String,
    val specifications: List<SpecificationDto>
)

data class SpecificationDto(
    val key: String,
    val value: String
)

interface ApiService {
    @GET("api/home-data")
    suspend fun getHomeData(): HomeDataResponse

    @GET("api/products/{id}")
    suspend fun getProductDetail(@Path("id") id: String): ProductDetailDto
}
