package com.example.launcher

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.launcher.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val prefs = Prefs(app)
    private val api = Api()
    private val downloader = Downloader(api.client, app.getExternalFilesDir(null)!!.resolve("gamedata"))

    val settings = prefs.settings.stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())
    val token = prefs.token.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _servers = MutableStateFlow<List<Server>>(emptyList())
    val servers = _servers.asStateFlow()
    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()
    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()
    private val _progress = MutableStateFlow<SyncProgress?>(null)
    val progress = _progress.asStateFlow()
    private var syncJob: Job? = null

    fun clearMessage() { _message.value = null }

    fun refreshServers() = launchSafe {
        _servers.value = api.servers(settings.value.apiUrl)
    }

    fun login(user: String, pass: String, register: Boolean, apiUrl: String) = launchSafe {
        val base = apiUrl.trim().trimEnd('/')
        prefs.saveSettings(settings.value.copy(apiUrl = base))
        val r = if (register) api.register(base, user, pass) else api.login(base, user, pass)
        prefs.saveToken(r.token)
        if (settings.value.nickname.isBlank()) {
            prefs.saveSettings(settings.value.copy(apiUrl = base, nickname = user))
        }
    }

    fun logout() = viewModelScope.launch { prefs.saveToken(null) }

    fun saveSettings(s: AppSettings) = viewModelScope.launch {
        prefs.saveSettings(s); _message.value = "Configurações salvas"
    }

    fun startSync() {
        if (syncJob?.isActive == true) return
        syncJob = launchSafe {
            val m = api.manifest(settings.value.apiUrl)
            downloader.sync(m) { _progress.value = it }
            prefs.saveInstalledVersion(m.version)
            _message.value = "Arquivos atualizados (v${m.version})"
        }
    }

    fun cancelSync() { syncJob?.cancel(); _progress.value = null }

    /** Abre o app do jogo já instalado, passando host/porta/nick como extras. */
    fun play(server: Server) {
        val ctx = getApplication<Application>()
        val s = settings.value
        val intent = ctx.packageManager.getLaunchIntentForPackage(s.gamePackage)
        if (s.gamePackage.isBlank() || intent == null) {
            _message.value = "Defina o pacote do jogo nas configurações"; return
        }
        intent.putExtra("host", server.ip)
            .putExtra("port", server.port)
            .putExtra("nickname", s.nickname)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ctx.startActivity(intent)
    }

    private fun launchSafe(block: suspend () -> Unit): Job = viewModelScope.launch {
        _loading.value = true
        try { block() }
        catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Exception) { _message.value = e.message ?: "Erro desconhecido" }
        finally { _loading.value = false; _progress.value = null }
    }
}
