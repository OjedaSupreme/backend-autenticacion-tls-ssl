package com.practica.u3.security.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequireHttpsFilter extends OncePerRequestFilter {

	private final boolean sslEnabled;

	private final int httpsPort;

	public RequireHttpsFilter(@Value("${server.ssl.enabled:false}") boolean sslEnabled,
			@Value("${server.port:8443}") int httpsPort) {
		this.sslEnabled = sslEnabled;
		this.httpsPort = httpsPort;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return !sslEnabled;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		if (request.isSecure()) {
			Object protocol = request.getAttribute("org.apache.tomcat.util.net.secure_protocol_version");
			Object cipher = request.getAttribute("jakarta.servlet.request.cipher_suite");
			SecurityAudit.debug("TLS_SESSION",
					"protocolo=" + protocol + " cipher=" + cipher + " path=" + request.getRequestURI());
			filterChain.doFilter(request, response);
			return;
		}
		String location = "https://" + request.getServerName() + ":" + httpsPort + request.getRequestURI();
		if (request.getQueryString() != null) {
			location = location + "?" + request.getQueryString();
		}
		SecurityAudit.warn("PLAIN_HTTP_REJECTED", "path=" + request.getRequestURI() + " redirect=" + location);
		response.setHeader("Location", location);
		JsonErrorWriter.write(response, HttpStatus.FOUND.value(), "HTTPS Required",
				"Las peticiones HTTP sin cifrar no están permitidas. Use HTTPS con TLS 1.2 o TLS 1.3.",
				request.getRequestURI());
	}

}
