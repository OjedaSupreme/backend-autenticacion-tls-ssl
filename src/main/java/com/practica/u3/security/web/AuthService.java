package com.practica.u3.security.web;

import com.practica.u3.security.config.SecurityProperties;
import com.practica.u3.security.security.JwtService;
import com.practica.u3.security.security.SecurityAudit;
import com.practica.u3.security.web.dto.AuthResponse;
import com.practica.u3.security.web.dto.LoginRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

	private final AuthenticationManager authenticationManager;

	private final UserDetailsService userDetailsService;

	private final JwtService jwtService;

	private final SecurityProperties properties;

	public AuthService(AuthenticationManager authenticationManager, UserDetailsService userDetailsService,
			JwtService jwtService, SecurityProperties properties) {
		this.authenticationManager = authenticationManager;
		this.userDetailsService = userDetailsService;
		this.jwtService = jwtService;
		this.properties = properties;
	}

	public AuthResponse login(LoginRequest request) {
		try {
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.username(), request.password()));
		}
		catch (AuthenticationException ex) {
			SecurityAudit.warn("LOGIN_FAILURE", "usuario=" + request.username());
			throw new BadCredentialsException("Usuario o contraseña incorrectos");
		}
		UserDetails user = userDetailsService.loadUserByUsername(request.username());
		SecurityAudit.info("LOGIN_SUCCESS", "usuario=" + user.getUsername());
		long expiresIn = Math.max(0, properties.getJwt().getExpirationMs() / 1000);
		return new AuthResponse(jwtService.generateToken(user), "Bearer", expiresIn);
	}

}
