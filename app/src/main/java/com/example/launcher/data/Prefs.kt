package com.example.launcher.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.store by preferencesDataStore("launcher")

class Prefs(private val context: Context) {
    private val kApi = stringPreferencesKey("api_url")
    private val kNick = stringPreferencesKey("nickname")
    private val kPkg = stringPreferencesKey("game_package")
    private val kToken = stringPreferencesKey("token")
    private val kVersion = stringPreferencesKey("installed_version")

    val settings: Flow<AppSettings> = context.store.data.map {
        val d = AppSettings()
        AppSettings(
            apiUrl = it[kApi] ?: d.apiUrl,
            nickname = it[kNick] ?: d.nickname,
            gamePackage = it[kPkg] ?: d.gamePackage,
        )
    }
    val token: Flow<String?> = context.store.data.map { it[kToken] }
    val installedVersion: Flow<String?> = context.store.data.map { it[kVersion] }

    suspend fun saveSettings(s: AppSettings) = context.store.edit {
        it[kApi] = s.apiUrl.trim().trimEnd('/')
        it[kNick] = s.nickname.trim()
        it[kPkg] = s.gamePackage.trim()
    }
    suspend fun saveToken(t: String?) = context.store.edit {
        if (t == null) it.remove(kToken) else it[kToken] = t
    }
    suspend fun saveInstalledVersion(v: String) = context.store.edit { it[kVersion] = v }
}
