# Apartado: HTTPS solo con TLS 1.2 y TLS 1.3, y rechazo de HTTP.
# Requiere el API en ejecución.
$ErrorActionPreference = "Stop"
$https = "https://localhost:8443/api/public/estado"
$http = "http://localhost:8080/api/public/estado"

function Show-Step($title) {
    Write-Output ""
    Write-Output "=== $title ==="
}

Show-Step "Canal HTTPS"
curl.exe -k -sS -D - $https -o "$env:TEMP\hu-estado.json"
Get-Content "$env:TEMP\hu-estado.json"

Show-Step "HTTP redirige a HTTPS"
curl.exe -k -sS -D - -o "$env:TEMP\hu-http.json" $http
Write-Output "CUERPO:"
Get-Content "$env:TEMP\hu-http.json"

Show-Step "TLS 1.2"
curl.exe -k -sS -w "http=%{http_code}`n" --tlsv1.2 --tls-max 1.2 $https

Show-Step "TLS 1.3"
curl.exe -k -sS -w "http=%{http_code}`n" --tlsv1.3 --tls-max 1.3 $https

Show-Step "TLS 1.0 debe fallar"
curl.exe -k -sS -o NUL -w "http=%{http_code}`n" --tlsv1.0 --tls-max 1.0 $https
if ($LASTEXITCODE -eq 0) {
    Write-Output "No se esperaba un handshake TLS 1.0 correcto."
    exit 1
}
Write-Output "Handshake TLS 1.0 rechazado, como se esperaba."
