package fpl.ph60001.techmart.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // 10.0.2.2 là địa chỉ IP đặc biệt của máy ảo Android để truy cập localhost của máy tính
    private const val BASE_URL = "http://10.0.2.2:3000/"
    // API lấy dữ liệu tỉnh/quận/phường Việt Nam
    private const val LOCATION_BASE_URL = "https://provinces.open-api.vn/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    val locationApiService: LocationApiService by lazy {
        Retrofit.Builder()
            .baseUrl(LOCATION_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LocationApiService::class.java)
    }
}
