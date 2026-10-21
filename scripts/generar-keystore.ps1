$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$keystore = Join-Path $root "src\main\resources\keystore.p12"
New-Item -ItemType Directory -Force -Path (Split-Path $keystore) | Out-Null
if (Test-Path $keystore) {
    Remove-Item $keystore -Force
}
& keytool -genkeypair `
    -alias practica-u3 `
    -keyalg RSA `
    -keysize 2048 `
    -storetype PKCS12 `
    -keystore $keystore `
    -validity 3650 `
    -storepass changeit `
    -keypass changeit `
    -dname "CN=localhost, OU=Desarrollo, O=PracticaU3, L=Local, ST=Local, C=MX" `
    -ext "SAN=dns:localhost,ip:127.0.0.1"
Write-Output "Keystore de desarrollo creado en $keystore"
