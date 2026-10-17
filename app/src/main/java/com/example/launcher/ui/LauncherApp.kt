package com.example.launcher.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.launcher.AppViewModel

private enum class Tab(val label: String) { Servers("Servidores"), Files("Arquivos"), Settings("Config.") }

@Composable
fun LauncherApp(vm: AppViewModel) {
    val token by vm.token.collectAsStateWithLifecycle()
    val message by vm.message.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var tab by remember { mutableStateOf(Tab.Servers) }

    LaunchedEffect(message) {
        message?.let { snackbar.showSnackbar(it); vm.clearMessage() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (token != null) NavigationBar {
                Tab.entries.forEach { t ->
                    NavigationBarItem(
                        selected = tab == t, onClick = { tab = t },
                        label = { Text(t.label) },
                        icon = {
                            Icon(
                                when (t) {
                                    Tab.Servers -> Icons.Default.Dns
                                    Tab.Files -> Icons.Default.Download
                                    Tab.Settings -> Icons.Default.Settings
                                }, null
                            )
                        },
                    )
                }
            }
        },
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            if (token == null) LoginScreen(vm)
            else when (tab) {
                Tab.Servers -> ServersScreen(vm)
                Tab.Files -> FilesScreen(vm)
                Tab.Settings -> SettingsScreen(vm)
            }
        }
    }
}
