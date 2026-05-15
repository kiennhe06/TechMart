package fpl.ph60001.techmart.cart.viewmodel

import androidx.lifecycle.ViewModel
import fpl.ph60001.techmart.network.ProductDetailDto
import fpl.ph60001.techmart.network.SimpleProductDto
import fpl.ph60001.techmart.utils.PreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class CartItem(
    val id: String,
    val name: String,
    val price: String,
    val image: String,
    var quantity: Int = 1
)

class CartViewModel(private val preferenceManager: PreferenceManager) : ViewModel() {
    private val _cartItems = MutableStateFlow<List<CartItem>>(preferenceManager.getCart())
    val cartItems: StateFlow<List<CartItem>> = _cartItems

    private val _favoriteIds = MutableStateFlow<Set<String>>(preferenceManager.getFavorites())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds

    private val _checkoutItems = MutableStateFlow<List<CartItem>>(emptyList())
    val checkoutItems: StateFlow<List<CartItem>> = _checkoutItems

    fun addToCart(product: ProductDetailDto) {
        val currentItems = _cartItems.value.toMutableList()
        val existingItem = currentItems.find { it.id == product.id }
        
        if (existingItem != null) {
            existingItem.quantity += 1
        } else {
            currentItems.add(CartItem(product.id, product.name, product.price, product.image))
        }
        _cartItems.value = currentItems
        preferenceManager.saveCart(currentItems)
    }

    fun toggleFavorite(productId: String) {
        val currentFavorites = _favoriteIds.value.toMutableSet()
        if (currentFavorites.contains(productId)) {
            currentFavorites.remove(productId)
        } else {
            currentFavorites.add(productId)
        }
        _favoriteIds.value = currentFavorites
        preferenceManager.saveFavorites(currentFavorites)
    }

    fun removeFromCart(productId: String) {
        val newList = _cartItems.value.filter { it.id != productId }
        _cartItems.value = newList
        preferenceManager.saveCart(newList)
    }

    fun updateQuantity(productId: String, delta: Int) {
        val currentItems = _cartItems.value.toMutableList()
        val index = currentItems.indexOfFirst { it.id == productId }
        if (index != -1) {
            val newQty = currentItems[index].quantity + delta
            if (newQty > 0) {
                currentItems[index] = currentItems[index].copy(quantity = newQty)
                _cartItems.value = currentItems
                preferenceManager.saveCart(currentItems)
            } else {
                removeFromCart(productId)
            }
        }
    }

    fun setCheckoutItems(items: List<CartItem>) {
        _checkoutItems.value = items
    }

    fun clearCart() {
        // Chỉ xóa những sản phẩm đã thanh toán khỏi giỏ hàng
        val checkoutIds = _checkoutItems.value.map { it.id }.toSet()
        val remainingItems = _cartItems.value.filter { !checkoutIds.contains(it.id) }
        
        _cartItems.value = remainingItems
        preferenceManager.saveCart(remainingItems)
        _checkoutItems.value = emptyList() // Xóa danh sách hàng chờ thanh toán
    }
}
