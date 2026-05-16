package com.example.coursemate.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.coursemate.network.retrofit.TokenReader
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")

class TokenDataStore(private val context: Context) : TokenReader {
    private val accessTokenKey = stringPreferencesKey("access_token")

    val token: Flow<String?> = context.authDataStore.data.map { preferences ->
        preferences[accessTokenKey]
    }

    override suspend fun currentToken(): String? = token.first()

    suspend fun saveToken(token: String) {
        context.authDataStore.edit { preferences ->
            preferences[accessTokenKey] = token
        }
    }

    suspend fun clearToken() {
        context.authDataStore.edit { preferences ->
            preferences.remove(accessTokenKey)
        }
    }
}
