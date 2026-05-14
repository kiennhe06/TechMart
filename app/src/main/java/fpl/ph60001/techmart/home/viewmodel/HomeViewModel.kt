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

    private val _selectedCategoryId = MutableStateFlow<Int?>(null)
    val selectedCategoryId: StateFlow<Int?> = _selectedCategoryId

    init {
        fetchHomeData()
    }

    fun selectCategory(categoryId: Int?) {
        _selectedCategoryId.value = if (_selectedCategoryId.value == categoryId) null else categoryId
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
