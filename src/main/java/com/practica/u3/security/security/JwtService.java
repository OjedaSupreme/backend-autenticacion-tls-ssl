package com.practica.u3.security.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.practica.u3.security.config.SecurityProperties;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class JwtService {

	private static final int MAX_TOKEN_LENGTH = 4096;

	private final byte[] secret;

	private final long expirationMs;

	public JwtService(SecurityProperties properties) {
		byte[] secretBytes = properties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8);
		if (secretBytes.length < 32) {
			throw new IllegalStateException("app.security.jwt.secret debe tener al menos 32 bytes");
		}
		this.secret = secretBytes;
		this.expirationMs = properties.getJwt().getExpirationMs();
	}

	public String generateToken(UserDetails user) {
		Instant now = Instant.now();
		List<String> roles = user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
		JWTClaimsSet claims = new JWTClaimsSet.Builder().subject(user.getUsername())
				.issueTime(Date.from(now))
				.expirationTime(Date.from(now.plusMillis(expirationMs)))
				.claim("roles", roles)
				.build();
		try {
			SignedJWT jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.HS256).build(), claims);
			jwt.sign(new MACSigner(secret));
			return jwt.serialize();
		}
		catch (JOSEException ex) {
			throw new IllegalStateException("No se pudo firmar el token JWT", ex);
		}
	}

	public ParsedToken parse(String token) {
		if (token == null || token.isBlank() || token.length() > MAX_TOKEN_LENGTH) {
			throw new BadCredentialsException("El token de autenticación no es válido");
		}
		try {
			SignedJWT jwt = SignedJWT.parse(token);
			if (!JWSAlgorithm.HS256.equals(jwt.getHeader().getAlgorithm())) {
				throw new BadCredentialsException("El algoritmo del token no está permitido");
			}
			if (!jwt.verify(new MACVerifier(secret))) {
				throw new BadCredentialsException("La firma del token no es válida");
			}
			JWTClaimsSet claims = jwt.getJWTClaimsSet();
			Date expiration = claims.getExpirationTime();
			if (expiration == null || !expiration.after(new Date())) {
				throw new BadCredentialsException("El token ha expirado");
			}
			String username = claims.getSubject();
			if (username == null || username.isBlank()) {
				throw new BadCredentialsException("El token de autenticación no es válido");
			}
			return new ParsedToken(username, authoritiesOf(claims));
		}
		catch (BadCredentialsException ex) {
			throw ex;
		}
		catch (ParseException | JOSEException ex) {
			throw new BadCredentialsException("El token de autenticación no es válido");
		}
	}

	private List<GrantedAuthority> authoritiesOf(JWTClaimsSet claims) throws ParseException {
		Object raw = claims.getClaim("roles");
		if (raw instanceof List<?> roles) {
			return roles.stream().map(role -> (GrantedAuthority) new SimpleGrantedAuthority(String.valueOf(role))).toList();
		}
		return List.of();
	}

	public record ParsedToken(String username, List<GrantedAuthority> authorities) {
	}

}
