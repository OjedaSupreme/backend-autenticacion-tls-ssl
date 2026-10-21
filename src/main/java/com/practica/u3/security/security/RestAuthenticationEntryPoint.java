package com.practica.u3.security.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException {
		String detail = authException.getMessage() == null ? "" : authException.getMessage();
		String message = switch (detail) {
			case "El token ha expirado", "La firma del token no es válida", "El algoritmo del token no está permitido",
					"El esquema de autenticación debe ser Bearer", "El token de autenticación no es válido" ->
				detail;
			default -> "Se requiere un token Bearer válido";
		};
		SecurityAudit.warn("UNAUTHENTICATED", "path=" + request.getRequestURI() + " motivo=" + message);
		JsonErrorWriter.write(response, HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase(),
				message, request.getRequestURI());
	}

}
