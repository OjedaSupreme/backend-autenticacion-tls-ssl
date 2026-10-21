# Apartados de HU-SEC-001

Scripts de cada apartado. El API debe estar detenido para generar el certificado y en ejecución (`.\mvnw.cmd spring-boot:run`) para las verificaciones.

| Apartado | Script |
| --- | --- |
| Certificado PKCS12 | `01-certificado-tls/generar-keystore.ps1` |
| TLS 1.2 / 1.3 y redirección HTTP | `02-cifrado-https/verificar-tls.ps1` |
| JWT, 401 y 403 | `03-autenticacion-jwt/verificar-jwt.ps1` |
| Mismas pruebas en Postman | `04-postman/HU-SEC-001.postman_collection.json` |

En Postman importa la colección y desactiva **Settings → SSL certificate verification**, porque el certificado de localhost es autofirmado.
