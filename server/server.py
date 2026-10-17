#!/usr/bin/env python3
"""API de teste para o launcher (somente biblioteca padrão do Python).
Uso:  python server.py        (porta 8080)
Arquivos para download: coloque dentro de server/files/ -> aparecem no /manifest.
"""
import hashlib, json, os, secrets
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.parse import quote

BASE = os.path.dirname(os.path.abspath(__file__))
FILES = os.path.join(BASE, "files")
USERS = os.path.join(BASE, "users.json")
PORT = int(os.environ.get("PORT", "8080"))

SERVERS = [
    {"name": "Servidor de Teste", "ip": "127.0.0.1", "port": 7777, "players": 3, "maxPlayers": 100},
    {"name": "Servidor 2", "ip": "127.0.0.1", "port": 7778, "players": 0, "maxPlayers": 50},
]
tokens = {}  # token -> usuário (some ao reiniciar o servidor)


def load_users():
    try:
        with open(USERS) as f:
            return json.load(f)
    except FileNotFoundError:
        return {}


def save_users(u):
    with open(USERS, "w") as f:
        json.dump(u, f)


def hash_pw(pw, salt):
    return hashlib.pbkdf2_hmac("sha256", pw.encode(), bytes.fromhex(salt), 100_000).hex()


def sha256_file(path):
    h = hashlib.sha256()
    with open(path, "rb") as f:
        for chunk in iter(lambda: f.read(65536), b""):
            h.update(chunk)
    return h.hexdigest()


def manifest(host):
    files = []
    for root, _, names in os.walk(FILES):
        for n in sorted(names):
            full = os.path.join(root, n)
            rel = os.path.relpath(full, FILES).replace(os.sep, "/")
            files.append({
                "path": rel,
                "url": f"http://{host}/files/{quote(rel)}",
                "sha256": sha256_file(full),
                "size": os.path.getsize(full),
            })
    ver = hashlib.sha256(json.dumps(files, sort_keys=True).encode()).hexdigest()[:8]
    return {"version": ver, "files": files}


class H(BaseHTTPRequestHandler):
    def _send(self, code, obj):
        data = json.dumps(obj).encode()
        self.send_response(code)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(data)))
        self.end_headers()
        self.wfile.write(data)

    def do_GET(self):
        if self.path == "/servers":
            return self._send(200, SERVERS)
        if self.path == "/manifest":
            return self._send(200, manifest(self.headers.get("Host", f"127.0.0.1:{PORT}")))
        if self.path.startswith("/files/"):
            from urllib.parse import unquote
            rel = unquote(self.path[len("/files/"):])
            full = os.path.realpath(os.path.join(FILES, rel))
            if not full.startswith(os.path.realpath(FILES) + os.sep) or not os.path.isfile(full):
                return self._send(404, {"error": "não encontrado"})
            self.send_response(200)
            self.send_header("Content-Type", "application/octet-stream")
            self.send_header("Content-Length", str(os.path.getsize(full)))
            self.end_headers()
            with open(full, "rb") as f:
                while chunk := f.read(65536):
                    self.wfile.write(chunk)
            return
        self._send(404, {"error": "não encontrado"})

    def do_POST(self):
        try:
            n = int(self.headers.get("Content-Length", 0))
            body = json.loads(self.rfile.read(n) or b"{}")
            user, pw = str(body.get("username", "")).strip(), str(body.get("password", ""))
        except Exception:
            return self._send(400, {"error": "JSON inválido"})
        if not user or len(pw) < 6:
            return self._send(400, {"error": "usuário vazio ou senha com menos de 6 caracteres"})
        users = load_users()
        key = user.lower()
        if self.path == "/auth/register":
            if key in users:
                return self._send(409, {"error": "usuário já existe"})
            salt = secrets.token_hex(16)
            users[key] = {"salt": salt, "hash": hash_pw(pw, salt)}
            save_users(users)
        elif self.path == "/auth/login":
            u = users.get(key)
            if not u or not secrets.compare_digest(u["hash"], hash_pw(pw, u["salt"])):
                return self._send(401, {"error": "usuário ou senha incorretos"})
        else:
            return self._send(404, {"error": "não encontrado"})
        tok = secrets.token_hex(24)
        tokens[tok] = key
        self._send(200, {"token": tok})

    def log_message(self, fmt, *a):
        print("%s %s" % (self.address_string(), fmt % a))


if __name__ == "__main__":
    os.makedirs(FILES, exist_ok=True)
    print(f"API rodando em http://127.0.0.1:{PORT}  (Ctrl+C para parar)")
    ThreadingHTTPServer(("127.0.0.1", PORT), H).serve_forever()
