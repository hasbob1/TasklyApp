package com.example.taskly.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    /** null = المستخدم ما اختارش، فنتبع theme السيستم. */
    val darkMode: Flow<Boolean?> = context.settingsDataStore.data.map { it[DARK_MODE_KEY] }

    suspend fun setDarkMode(enabled: Boolean) {
        context.settingsDataStore.edit { it[DARK_MODE_KEY] = enabled }
    }

    private companion object {
        val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
    }
}