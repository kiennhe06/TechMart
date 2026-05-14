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
}
