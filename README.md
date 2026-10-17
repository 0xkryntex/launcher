# Launcher (Kotlin + Compose)

Abra a pasta no Android Studio (ele gera o Gradle wrapper) e rode.

## Contrato da sua API
- GET  /servers       -> [{"name","ip","port","players","maxPlayers"}]
- POST /auth/login    -> {"token"}   (body: {"username","password"})
- POST /auth/register -> {"token"}
- GET  /manifest      -> {"version","files":[{"path","url","sha256","size"}]}

## Arquivos
- data/Api.kt        chamadas HTTP
- data/Downloader.kt sync com SHA-256 e proteção contra path traversal
- data/Prefs.kt      DataStore (URL, nick, pacote do jogo, token)
- AppViewModel.kt    estado e ações
- ui/*               telas: Login, Servidores, Arquivos, Config.

O botão de jogar abre o app do jogo (pacote definido em Config.) via intent
com extras host/port/nickname. Adapte ao seu cliente.
