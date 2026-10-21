package com.practica.u3.security.web;

import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class PublicController {

	@GetMapping("/estado")
	public Map<String, Object> estado(HttpServletRequest request) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("estado", "ok");
		body.put("seguro", request.isSecure());
		body.put("protocoloTls", request.getAttribute("org.apache.tomcat.util.net.secure_protocol_version"));
		body.put("cipher", request.getAttribute("jakarta.servlet.request.cipher_suite"));
		return body;
	}

}
