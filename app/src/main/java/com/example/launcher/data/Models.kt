package com.example.launcher.data

import kotlinx.serialization.Serializable

@Serializable
data class Server(
    val name: String,
    val ip: String,
    val port: Int,
    val players: Int = 0,
    val maxPlayers: Int = 0,
)

@Serializable
data class AuthRequest(val username: String, val password: String)

@Serializable
data class AuthResponse(val token: String)

@Serializable
data class RemoteFile(
    val path: String,    // caminho relativo dentro da pasta de dados do jogo
    val url: String,
    val sha256: String,
    val size: Long,
)

@Serializable
data class Manifest(val version: String, val files: List<RemoteFile>)

data class AppSettings(
    val apiUrl: String = "https://api.seudominio.com",
    val nickname: String = "",
    val gamePackage: String = "",
)
