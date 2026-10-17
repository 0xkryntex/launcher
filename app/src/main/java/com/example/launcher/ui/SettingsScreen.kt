package com.example.launcher.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.launcher.AppViewModel

@Composable
fun SettingsScreen(vm: AppViewModel) {
    val saved by vm.settings.collectAsStateWithLifecycle()
    var api by remember(saved) { mutableStateOf(saved.apiUrl) }
    var nick by remember(saved) { mutableStateOf(saved.nickname) }
    var pkg by remember(saved) { mutableStateOf(saved.gamePackage) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Configurações", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(api, { api = it }, label = { Text("URL da API (https)") },
            singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(nick, { nick = it }, label = { Text("Nickname") },
            singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(pkg, { pkg = it }, label = { Text("Pacote do jogo instalado") },
            singleLine = true, modifier = Modifier.fillMaxWidth())
        Button(
            onClick = { vm.saveSettings(saved.copy(apiUrl = api, nickname = nick, gamePackage = pkg)) },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Salvar") }
        OutlinedButton(onClick = vm::logout, modifier = Modifier.fillMaxWidth()) { Text("Sair da conta") }
    }
}
