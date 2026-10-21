package com.practica.u3.security.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;

	private final RestAuthenticationEntryPoint authenticationEntryPoint;

	public JwtAuthenticationFilter(JwtService jwtService, RestAuthenticationEntryPoint authenticationEntryPoint) {
		this.jwtService = jwtService;
		this.authenticationEntryPoint = authenticationEntryPoint;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header == null || header.isBlank()) {
			filterChain.doFilter(request, response);
			return;
		}
		if (!header.startsWith("Bearer ")) {
			reject(request, response, "El esquema de autenticación debe ser Bearer");
			return;
		}
		String token = header.substring(7).trim();
		try {
			JwtService.ParsedToken parsed = jwtService.parse(token);
			var authentication = new UsernamePasswordAuthenticationToken(parsed.username(), null, parsed.authorities());
			SecurityContextHolder.getContext().setAuthentication(authentication);
			filterChain.doFilter(request, response);
		}
		catch (BadCredentialsException ex) {
			SecurityContextHolder.clearContext();
			SecurityAudit.warn("TOKEN_REJECTED", "path=" + request.getRequestURI() + " motivo=" + ex.getMessage());
			authenticationEntryPoint.commence(request, response, ex);
		}
	}

	private void reject(HttpServletRequest request, HttpServletResponse response, String message)
			throws IOException, ServletException {
		SecurityContextHolder.clearContext();
		SecurityAudit.warn("TOKEN_REJECTED", "path=" + request.getRequestURI() + " motivo=" + message);
		authenticationEntryPoint.commence(request, response, new BadCredentialsException(message));
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();
		return path != null && path.startsWith("/api/public/");
	}

}
