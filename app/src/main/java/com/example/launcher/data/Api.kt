package com.example.launcher.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

/**
 * Cliente da SUA API. Endpoints esperados:
 *   GET  /servers            -> [Server]
 *   POST /auth/login         -> AuthResponse
 *   POST /auth/register      -> AuthResponse
 *   GET  /manifest           -> Manifest
 */
class Api(val client: OkHttpClient = OkHttpClient()) {
    val json = Json { ignoreUnknownKeys = true }
    private val jsonType = "application/json".toMediaType()

    suspend fun servers(base: String): List<Server> =
        json.decodeFromString(get("$base/servers"))

    suspend fun manifest(base: String): Manifest =
        json.decodeFromString(get("$base/manifest"))

    suspend fun login(base: String, user: String, pass: String): AuthResponse =
        json.decodeFromString(post("$base/auth/login", AuthRequest(user, pass)))

    suspend fun register(base: String, user: String, pass: String): AuthResponse =
        json.decodeFromString(post("$base/auth/register", AuthRequest(user, pass)))

    private suspend fun get(url: String): String = withContext(Dispatchers.IO) {
        client.newCall(Request.Builder().url(url).build()).execute().use { it.bodyOrThrow() }
    }

    private suspend fun post(url: String, body: AuthRequest): String = withContext(Dispatchers.IO) {
        val req = Request.Builder().url(url)
            .post(json.encodeToString(body).toRequestBody(jsonType)).build()
        client.newCall(req).execute().use { it.bodyOrThrow() }
    }

    private fun okhttp3.Response.bodyOrThrow(): String {
        val text = body?.string().orEmpty()
        if (!isSuccessful) throw IOException("HTTP $code: ${text.take(200)}")
        return text
    }
}
