package fpl.ph60001.techmart.utils

import android.content.Context
import android.content.SharedPreferences

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import fpl.ph60001.techmart.cart.viewmodel.CartItem

class PreferenceManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("TechMartPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveLoginState(isRemembered: Boolean, email: String? = null) {
        sharedPreferences.edit().apply {
            putBoolean("is_remembered", isRemembered)
            putString("user_email", email)
            apply()
        }
    }

    fun isRemembered(): Boolean {
        return sharedPreferences.getBoolean("is_remembered", false)
    }

    fun getSavedEmail(): String? {
        return sharedPreferences.getString("user_email", null)
    }

    fun saveFavorites(favoriteIds: Set<String>) {
        val json = gson.toJson(favoriteIds)
        sharedPreferences.edit().putString("favorite_ids", json).apply()
    }

    fun getFavorites(): Set<String> {
        val json = sharedPreferences.getString("favorite_ids", null) ?: return emptySet()
        val type = object : TypeToken<Set<String>>() {}.type
        return gson.fromJson(json, type)
    }

    fun saveCart(cartItems: List<CartItem>) {
        val json = gson.toJson(cartItems)
        sharedPreferences.edit().putString("cart_items", json).apply()
    }

    fun getCart(): List<CartItem> {
        val json = sharedPreferences.getString("cart_items", null) ?: return emptyList()
        val type = object : TypeToken<List<CartItem>>() {}.type
        return gson.fromJson(json, type)
    }

    fun clearLoginState() {
        sharedPreferences.edit().clear().apply()
    }

    // --- Lưu và đọc thông tin giao hàng để auto-fill ---
    fun saveShippingInfo(
        name: String,
        phone: String,
        province: String,
        district: String,
        ward: String,
        addressDetail: String
    ) {
        sharedPreferences.edit().apply {
            putString("shipping_name", name)
            putString("shipping_phone", phone)
            putString("shipping_province", province)
            putString("shipping_district", district)
            putString("shipping_ward", ward)
            putString("shipping_address_detail", addressDetail)
            apply()
        }
    }

    fun getShippingName(): String = sharedPreferences.getString("shipping_name", "") ?: ""
    fun getShippingPhone(): String = sharedPreferences.getString("shipping_phone", "") ?: ""
    fun getShippingProvince(): String = sharedPreferences.getString("shipping_province", "") ?: ""
    fun getShippingDistrict(): String = sharedPreferences.getString("shipping_district", "") ?: ""
    fun getShippingWard(): String = sharedPreferences.getString("shipping_ward", "") ?: ""
    fun getShippingAddressDetail(): String = sharedPreferences.getString("shipping_address_detail", "") ?: ""
}
