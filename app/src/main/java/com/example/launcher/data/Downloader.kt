package com.example.launcher.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import kotlin.coroutines.coroutineContext

data class SyncProgress(val doneFiles: Int, val totalFiles: Int, val current: String)

/** Baixa apenas os arquivos novos/alterados e confere o SHA-256 de cada um. */
class Downloader(private val client: OkHttpClient, private val root: File) {

    suspend fun sync(manifest: Manifest, onProgress: (SyncProgress) -> Unit) =
        withContext(Dispatchers.IO) {
            root.mkdirs()
            val rootCanon = root.canonicalFile
            manifest.files.forEachIndexed { index, f ->
                coroutineContext.ensureActive()
                onProgress(SyncProgress(index, manifest.files.size, f.path))

                val target = File(rootCanon, f.path).canonicalFile
                // evita path traversal vindo de um manifesto malicioso
                if (!target.path.startsWith(rootCanon.path + File.separator)) {
                    throw IOException("Caminho inválido no manifesto: ${f.path}")
                }
                if (target.exists() && sha256(target).equals(f.sha256, true)) return@forEachIndexed

                target.parentFile?.mkdirs()
                val part = File(target.path + ".part")
                download(f.url, part)
                if (!sha256(part).equals(f.sha256, true)) {
                    part.delete()
                    throw IOException("Hash incorreto: ${f.path}")
                }
                if (target.exists()) target.delete()
                part.renameTo(target)
            }
            onProgress(SyncProgress(manifest.files.size, manifest.files.size, ""))
        }

    private suspend fun download(url: String, out: File) {
        client.newCall(Request.Builder().url(url).build()).execute().use { resp ->
            if (!resp.isSuccessful) throw IOException("HTTP ${resp.code} em $url")
            resp.body!!.byteStream().use { input ->
                out.outputStream().use { output ->
                    val buf = ByteArray(64 * 1024)
                    while (true) {
                        coroutineContext.ensureActive()
                        val n = input.read(buf)
                        if (n < 0) break
                        output.write(buf, 0, n)
                    }
                }
            }
        }
    }

    private fun sha256(file: File): String {
        val md = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { s ->
            val buf = ByteArray(64 * 1024)
            while (true) { val n = s.read(buf); if (n < 0) break; md.update(buf, 0, n) }
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }
}
