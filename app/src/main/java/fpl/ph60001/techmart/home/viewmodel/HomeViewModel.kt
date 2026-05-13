package fpl.ph60001.techmart.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fpl.ph60001.techmart.network.HomeDataResponse
import fpl.ph60001.techmart.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val _homeData = MutableStateFlow<HomeDataResponse?>(null)
    val homeData: StateFlow<HomeDataResponse?> = _homeData

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        fetchHomeData()
    }

    fun fetchHomeData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitClient.apiService.getHomeData()
                _homeData.value = response
            } catch (e: Exception) {
                e.printStackTrace()
                // Xử lý lỗi ở đây (vd: hiện thông báo lỗi)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
