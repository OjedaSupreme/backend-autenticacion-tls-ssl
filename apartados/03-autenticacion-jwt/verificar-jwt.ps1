# Apartado: login JWT, 401 sin token o con token inválido, y 403 sin rol ADMIN.
# Requiere el API en ejecución.
$ErrorActionPreference = "Stop"
$base = "https://localhost:8443"

function Invoke-Api($method, $path, $body, $token) {
    $args = @("-k", "-sS", "-w", "`nhttp=%{http_code}`n", "-X", $method, "$base$path")
    if ($body) {
        $file = Join-Path $env:TEMP ("hu-" + [guid]::NewGuid().ToString() + ".json")
        Set-Content -Path $file -Value $body -Encoding ascii
        $args += @("-H", "Content-Type: application/json", "--data-binary", "@$file")
    }
    if ($token) {
        $args += @("-H", "Authorization: Bearer $token")
    }
    & curl.exe @args
}

function Read-Token($json) {
    return ($json | ConvertFrom-Json).token
}

Write-Output "=== Login user ==="
$userBody = curl.exe -k -sS -X POST "$base/api/auth/login" -H "Content-Type: application/json" --data-binary '{"username":"user","password":"password"}'
Write-Output $userBody
$userToken = Read-Token $userBody

Write-Output "`n=== Login admin ==="
$adminBody = curl.exe -k -sS -X POST "$base/api/auth/login" -H "Content-Type: application/json" --data-binary '{"username":"admin","password":"admin123"}'
Write-Output $adminBody
$adminToken = Read-Token $adminBody

Write-Output "`n=== Recurso sin token (401) ==="
Invoke-Api "GET" "/api/recursos"

Write-Output "`n=== Token inválido (401) ==="
Invoke-Api "GET" "/api/recursos" $null "token-invalido"

Write-Output "`n=== Recurso con token de user (200) ==="
Invoke-Api "GET" "/api/recursos" $null $userToken

Write-Output "`n=== Panel admin con token de user (403) ==="
Invoke-Api "GET" "/api/admin/panel" $null $userToken

Write-Output "`n=== Panel admin con token de admin (200) ==="
Invoke-Api "GET" "/api/admin/panel" $null $adminToken

Write-Output "`n=== Contraseña incorrecta (401) ==="
Invoke-Api "POST" "/api/auth/login" '{"username":"user","password":"incorrecta"}'
