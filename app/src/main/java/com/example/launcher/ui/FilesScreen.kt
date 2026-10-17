package com.example.launcher.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.launcher.AppViewModel

@Composable
fun FilesScreen(vm: AppViewModel) {
    val progress by vm.progress.collectAsStateWithLifecycle()
    val loading by vm.loading.collectAsStateWithLifecycle()

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Arquivos do jogo", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(20.dp))
        val p = progress
        if (p != null && p.totalFiles > 0) {
            LinearProgressIndicator(
                progress = { p.doneFiles / p.totalFiles.toFloat() },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            Text("${p.doneFiles}/${p.totalFiles}  ${p.current}", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = vm::cancelSync) { Text("Cancelar") }
        } else {
            Button(onClick = vm::startSync, enabled = !loading) { Text("Verificar / atualizar") }
        }
    }
}
