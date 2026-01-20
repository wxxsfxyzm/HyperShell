package app.hypershell.data.settings.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AppDataStore(
    private val dataStore: DataStore<Preferences>,
    // private val json: Json
) {
    companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val THEME_PALETTE_STYLE = stringPreferencesKey("theme_palette_style")
        val THEME_USE_DYNAMIC_COLOR = booleanPreferencesKey("theme_use_dynamic_color")
        val THEME_SEED_COLOR = intPreferencesKey("theme_seed_color")
        val UI_USE_MIUIX = booleanPreferencesKey("ui_use_miui_x")
        val UI_USE_MIUIX_MONET = booleanPreferencesKey("ui_use_miui_x_monet")

        val TERMINAL_FONT_SIZE = intPreferencesKey("terminal_font_size")
        val TERMINAL_MAX_LINES = intPreferencesKey("terminal_max_lines")

        // [新增] 特权模式后端 (存储 Enum 的 name 字符串)
        val TERMINAL_MODE = stringPreferencesKey("terminal_mode")
        val PRIVILEGED_BACKEND = stringPreferencesKey("privileged_backend")
    }

    suspend fun putString(key: Preferences.Key<String>, value: String) {
        dataStore.edit { it[key] = value }
    }

    fun getString(key: Preferences.Key<String>, default: String = ""): Flow<String> =
        dataStore.data.map { it[key] ?: default }

    suspend fun putInt(key: Preferences.Key<Int>, value: Int) {
        dataStore.edit { it[key] = value }
    }

    fun getInt(key: Preferences.Key<Int>, default: Int = 0): Flow<Int> =
        dataStore.data.map { it[key] ?: default }

    suspend fun putBoolean(key: Preferences.Key<Boolean>, value: Boolean) {
        dataStore.edit { it[key] = value }
    }

    fun getBoolean(key: Preferences.Key<Boolean>, default: Boolean = false): Flow<Boolean> =
        dataStore.data.map { it[key] ?: default }
}