package com.youtubecc.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("ytcc_prefs")

class PreferencesManager(private val context: Context) {
    private val keyApi = stringPreferencesKey("api_key")
    private val keyChannel = stringPreferencesKey("channel_id")

    val apiKeyFlow: Flow<String> = context.dataStore.data.map { it[keyApi] ?: "" }
    val channelIdFlow: Flow<String> = context.dataStore.data.map { it[keyChannel] ?: "" }

    suspend fun getApiKey(): String = apiKeyFlow.first()
    suspend fun getChannelId(): String = channelIdFlow.first()

    suspend fun setApiKey(value: String) {
        context.dataStore.edit { it[keyApi] = value.trim() }
    }

    suspend fun setChannelId(value: String) {
        context.dataStore.edit { it[keyChannel] = value.trim() }
    }
}
