package com.practica.u3.security.config;

import com.practica.u3.security.security.SecurityAudit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "server.ssl.enabled", havingValue = "true")
public class TlsStartupLogger implements ApplicationRunner {

	private final int httpsPort;

	private final int httpPort;

	public TlsStartupLogger(@Value("${server.port}") int httpsPort,
			@Value("${server.http.port:8080}") int httpPort) {
		this.httpsPort = httpsPort;
		this.httpPort = httpPort;
	}

	@Override
	public void run(ApplicationArguments args) {
		SecurityAudit.info("TLS_READY", "HTTPS en el puerto " + httpsPort
				+ " con protocolos TLSv1.2 y TLSv1.3. El puerto HTTP " + httpPort + " redirige a HTTPS.");
	}

}
