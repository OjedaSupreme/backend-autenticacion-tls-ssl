# HU-SEC-001 — Autenticación y cifrado TLS/SSL

API backend en Java 21 y Spring Boot que protege los recursos con Spring Security y JWT, y solo acepta tráfico cifrado con TLS 1.2 o TLS 1.3.

## Requisitos

- JDK 21
- El wrapper de Maven incluido (`mvnw.cmd`) descarga Maven la primera vez

## Arranque

```powershell
.\mvnw.cmd spring-boot:run
```

| Canal | Dirección |
| --- | --- |
| HTTPS | `https://localhost:8443` |
| HTTP | `http://localhost:8080` (redirige a HTTPS) |

El certificado de desarrollo está en `src/main/resources/keystore.p12` (PKCS12, alias `practica-u3`, contraseña `changeit`, CN `localhost`). Es autofirmado y solo sirve para el entorno local. Para regenerarlo:

```powershell
.\scripts\generar-keystore.ps1
```

La contraseña y los protocolos quedan en `src/main/resources/application.properties`:

```properties
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-type=PKCS12
server.ssl.key-store-password=${SSL_KEY_STORE_PASSWORD:changeit}
server.ssl.enabled-protocols=TLSv1.2,TLSv1.3
```

## Usuarios de desarrollo

| Usuario | Contraseña | Rol |
| --- | --- | --- |
| `user` | `password` | `USER` |
| `admin` | `admin123` | `ADMIN` |

El secreto HMAC del JWT también es de desarrollo. En otro entorno se sustituye con la variable `JWT_SECRET` (mínimo 32 bytes).

## Endpoints

| Método y ruta | Acceso |
| --- | --- |
| `GET /api/public/estado` | Público. Muestra si el canal es TLS |
| `POST /api/auth/login` | Público. Devuelve un Bearer token |
| `GET /api/recursos` | Token válido |
| `GET /api/admin/panel` | Token con rol `ADMIN` |

El cliente Angular en `http://localhost:4200` o `https://localhost:4200` está permitido por CORS.

## Verificación manual

```powershell
curl.exe -k https://localhost:8443/api/public/estado
curl.exe -k -i http://localhost:8080/api/public/estado
curl.exe -k -i --tlsv1.2 --tls-max 1.2 https://localhost:8443/api/public/estado
curl.exe -k -i --tls-max 1.0 https://localhost:8443/api/public/estado
```

La última llamada debe fallar el handshake: el servidor no negocia TLS 1.0 ni TLS 1.1. Un fallo de handshake ocurre antes de HTTP; el cliente recibe la alerta TLS y el servidor deja el evento en el registro. Si el contenedor convierte el fallo en una respuesta HTTP, `/error` devuelve el mensaje `No se pudo completar el handshake TLS. Use TLS 1.2 o TLS 1.3.`

```powershell
curl.exe -k -X POST https://localhost:8443/api/auth/login -H "Content-Type: application/json" -d "{\"username\":\"user\",\"password\":\"password\"}"
curl.exe -k https://localhost:8443/api/recursos
curl.exe -k https://localhost:8443/api/recursos -H "Authorization: Bearer TOKEN"
curl.exe -k https://localhost:8443/api/admin/panel -H "Authorization: Bearer TOKEN_USER"
curl.exe -k https://localhost:8443/api/admin/panel -H "Authorization: Bearer TOKEN_ADMIN"
```

Sin token, la API responde `401`. Con un token sin rol de administración, `403`. Los eventos quedan en el logger `SECURITY_AUDIT` (`LOGIN_SUCCESS`, `LOGIN_FAILURE`, `TOKEN_REJECTED`, `UNAUTHENTICATED`, `ACCESS_DENIED`, `PLAIN_HTTP_REJECTED`).

## Pruebas

```powershell
.\mvnw.cmd test
```
