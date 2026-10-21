package com.practica.u3.security.web;

import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ResourceController {

	@GetMapping("/recursos")
	public Map<String, String> recursos(Authentication authentication) {
		return Map.of("mensaje", "Acceso autorizado al recurso protegido", "usuario", authentication.getName());
	}

	@GetMapping("/admin/panel")
	public Map<String, String> admin(Authentication authentication) {
		return Map.of("mensaje", "Acceso autorizado al panel de administración", "usuario", authentication.getName());
	}

}
