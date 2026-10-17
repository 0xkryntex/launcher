package com.example.launcher.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.launcher.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServersScreen(vm: AppViewModel) {
    val servers by vm.servers.collectAsStateWithLifecycle()
    val loading by vm.loading.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { vm.refreshServers() }

    PullToRefreshBox(isRefreshing = loading, onRefresh = { vm.refreshServers() }) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (servers.isEmpty() && !loading) item { Text("Nenhum servidor. Puxe para atualizar.") }
            items(servers) { s ->
                Card(Modifier.fillMaxWidth().clickable { vm.play(s) }) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(s.name, style = MaterialTheme.typography.titleMedium)
                            Text("${s.ip}:${s.port}", style = MaterialTheme.typography.bodySmall)
                        }
                        Text("${s.players}/${s.maxPlayers}")
                    }
                }
            }
        }
    }
}
