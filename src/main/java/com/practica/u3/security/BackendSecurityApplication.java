package com.practica.u3.security;

import com.practica.u3.security.config.SecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(SecurityProperties.class)
public class BackendSecurityApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendSecurityApplication.class, args);
	}

}
