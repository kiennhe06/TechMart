package fpl.ph60001.techmart.network

import retrofit2.http.GET
import retrofit2.http.Path

// Dữ liệu tỉnh/thành phố
data class ProvinceDto(
    val name: String,
    val code: Int,
    val wards: List<WardDto>? = null
)

// Dữ liệu phường/xã
data class WardDto(
    val name: String,
    val code: Int
)

// API lấy dữ liệu địa chỉ Việt Nam (provinces.open-api.vn)
interface LocationApiService {
    @GET("api/v2/")
    suspend fun getProvinces(): List<ProvinceDto>

    @GET("api/v2/p/{code}?depth=2")
    suspend fun getWardsByProvince(@Path("code") provinceCode: Int): ProvinceDto
}
