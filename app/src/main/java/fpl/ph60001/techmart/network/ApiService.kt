package fpl.ph60001.techmart.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PATCH
import retrofit2.http.Path
import fpl.ph60001.techmart.order.Order

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
    val image: String,
    val stock: Int = 0
)

data class SimpleProductDto(
    val id: String,
    val name: String,
    val price: String,
    val image: String,
    val categoryId: Int,
    val stock: Int = 0
)

data class ProductDetailDto(
    val id: String,
    val name: String,
    val price: String,
    val image: String,
    val stock: Int = 0,
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

    @POST("api/orders/create")
    suspend fun createOrder(@Body order: Order): Response<Unit>

    @GET("api/orders")
    suspend fun getOrders(): List<Order>

    @PATCH("api/orders/{id}/cancel")
    suspend fun cancelOrder(@Path("id") id: String): Response<Unit>

    @POST("api/orders/{id}/review")
    suspend fun submitReview(@Path("id") id: String, @Body review: fpl.ph60001.techmart.order.OrderReview): Response<Unit>
}
