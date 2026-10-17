package com.example.launcher.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.launcher.AppViewModel

@Composable
fun LoginScreen(vm: AppViewModel) {
    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var register by remember { mutableStateOf(false) }
    val loading by vm.loading.collectAsStateWithLifecycle()
    val saved by vm.settings.collectAsStateWithLifecycle()
    var api by remember(saved.apiUrl) { mutableStateOf(saved.apiUrl) }

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(if (register) "Criar conta" else "Entrar", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(api, { api = it }, label = { Text("URL da API") }, singleLine = true,
            modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(user, { user = it }, label = { Text("Usuário") }, singleLine = true,
            modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(pass, { pass = it }, label = { Text("Senha") }, singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = { vm.login(user.trim(), pass, register, api) },
            enabled = !loading && api.isNotBlank() && user.isNotBlank() && pass.length >= 6,
            modifier = Modifier.fillMaxWidth(),
        ) { Text(if (loading) "Aguarde..." else if (register) "Registrar" else "Entrar") }
        TextButton(onClick = { register = !register }) {
            Text(if (register) "Já tenho conta" else "Criar conta")
        }
    }
}
