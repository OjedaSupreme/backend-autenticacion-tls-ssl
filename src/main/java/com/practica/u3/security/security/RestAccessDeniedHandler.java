package com.practica.u3.security.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException {
		String user = request.getUserPrincipal() == null ? "desconocido" : request.getUserPrincipal().getName();
		SecurityAudit.warn("ACCESS_DENIED", "usuario=" + user + " path=" + request.getRequestURI());
		JsonErrorWriter.write(response, HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.getReasonPhrase(),
				"No cuenta con permisos suficientes para este recurso", request.getRequestURI());
	}

}
