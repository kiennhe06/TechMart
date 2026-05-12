package fpl.ph60001.techmart.utils

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("TechMartPrefs", Context.MODE_PRIVATE)

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

    fun clearLoginState() {
        sharedPreferences.edit().clear().apply()
    }
}
