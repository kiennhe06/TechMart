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

// User Request & Response Models
data class RegisterRequest(
    val name: String,
    val email: String,
    val phone: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class SocialLoginRequest(
    val name: String,
    val email: String
)

data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val phone: String
)

data class AuthResponse(
    val message: String,
    val user: UserProfile?,
    val error: String? = null
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

    @GET("api/orders/user/{email}")
    suspend fun getOrdersByEmail(@Path("email") email: String): List<Order>

    @PATCH("api/orders/{id}/cancel")
    suspend fun cancelOrder(@Path("id") id: String): Response<Unit>

    @POST("api/orders/{id}/review")
    suspend fun submitReview(@Path("id") id: String, @Body review: fpl.ph60001.techmart.order.OrderReview): Response<Unit>

    @POST("users/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("users/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("users/social-login")
    suspend fun socialLogin(@Body request: SocialLoginRequest): Response<AuthResponse>

    @GET("users/profile/{email}")
    suspend fun getUserProfile(@Path("email") email: String): Response<AuthResponse>
}
