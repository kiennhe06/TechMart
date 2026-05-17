package fpl.ph60001.techmart.order

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class OrderItem(
    val name: String,
    val price: String,
    val image: String,
    val quantity: Int
)

data class OrderReview(
    val rating: Int, // 1-5 sao
    val comment: String,
    val adminReply: String? = null,
    val date: Long = System.currentTimeMillis()
)

data class Order(
    val id: String,
    val items: List<OrderItem>,
    val total: String,
    val address: String,
    val paymentMethod: String,
    val customerName: String = "",
    val customerEmail: String = "",
    val customerPhone: String = "",
    val orderDate: Long = System.currentTimeMillis(),
    val status: Int = 0, // -1=Đã hủy, 0=Đã đặt, 1=Đang xử lý, 2=Đang giao, 3=Đã giao
    val review: OrderReview? = null
)

class OrderRepository(context: Context) {
    private val prefs = context.getSharedPreferences("TechMartOrders", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveOrder(order: Order) {
        val orders = getOrders().toMutableList()
        orders.add(0, order)
        prefs.edit().putString("orders", gson.toJson(orders)).apply()
    }

    fun getOrders(): List<Order> {
        val json = prefs.getString("orders", null) ?: return emptyList()
        val type = object : TypeToken<List<Order>>() {}.type
        return gson.fromJson(json, type)
    }

    fun clearLocalOrders() {
        prefs.edit().remove("orders").apply()
    }
}
