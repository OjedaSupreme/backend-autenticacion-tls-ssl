package com.practica.u3.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.practica.u3.security.config.SecurityProperties;
import com.practica.u3.security.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

class JwtServiceTest {

	@Test
	void tokenFirmadoSePuedeLeer() {
		JwtService service = serviceWithExpiration(60_000);
		UserDetails user = User.withUsername("user").password("password").roles("USER").build();

		String token = service.generateToken(user);
		JwtService.ParsedToken parsed = service.parse(token);

		assertThat(parsed.username()).isEqualTo("user");
		assertThat(parsed.authorities()).extracting("authority").containsExactly("ROLE_USER");
	}

	@Test
	void tokenAlteradoSeRechaza() {
		JwtService service = serviceWithExpiration(60_000);
		UserDetails user = User.withUsername("user").password("password").roles("USER").build();
		String token = service.generateToken(user);
		String tampered = token.substring(0, token.length() - 2) + "aa";

		assertThatThrownBy(() -> service.parse(tampered)).isInstanceOf(BadCredentialsException.class);
	}

	@Test
	void tokenExpiradoSeRechaza() {
		JwtService service = serviceWithExpiration(-5_000);
		UserDetails user = User.withUsername("user").password("password").roles("USER").build();
		String token = service.generateToken(user);

		assertThatThrownBy(() -> service.parse(token)).isInstanceOf(BadCredentialsException.class)
				.hasMessage("El token ha expirado");
	}

	private JwtService serviceWithExpiration(long expirationMs) {
		SecurityProperties properties = new SecurityProperties();
		properties.getJwt().setSecret("practica-u3-test-jwt-secret-key-32bytes");
		properties.getJwt().setExpirationMs(expirationMs);
		return new JwtService(properties);
	}

}
