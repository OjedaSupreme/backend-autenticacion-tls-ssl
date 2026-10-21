package com.practica.u3.security.config;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.tomcat.TomcatWebServerFactory;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "server.ssl.enabled", havingValue = "true")
public class HttpsRedirectConfig {

	@Bean
	public WebServerFactoryCustomizer<TomcatServletWebServerFactory> httpConnector(
			@Value("${server.port}") int httpsPort, @Value("${server.http.port:8080}") int httpPort) {
		return factory -> {
			Connector connector = new Connector(TomcatWebServerFactory.DEFAULT_PROTOCOL);
			connector.setScheme("http");
			connector.setPort(httpPort);
			connector.setSecure(false);
			connector.setRedirectPort(httpsPort);
			factory.addAdditionalConnectors(connector);
		};
	}

}
