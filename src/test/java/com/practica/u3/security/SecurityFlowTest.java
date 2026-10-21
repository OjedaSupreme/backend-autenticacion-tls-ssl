package com.practica.u3.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "server.ssl.enabled=false")
class SecurityFlowTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void estadoPublicoNoRequiereToken() throws Exception {
		mockMvc.perform(get("/api/public/estado").header(HttpHeaders.ORIGIN, "http://localhost:4200"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.estado").value("ok"))
				.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4200"));
	}

	@Test
	void preflightCorsDesdeAngularEstaPermitido() throws Exception {
		mockMvc.perform(options("/api/auth/login").header(HttpHeaders.ORIGIN, "http://localhost:4200")
				.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
				.header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "content-type,authorization"))
				.andExpect(status().isOk())
				.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4200"));
	}

	@Test
	void loginValidoDevuelveBearerToken() throws Exception {
		mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"user\",\"password\":\"password\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.type").value("Bearer"))
				.andExpect(jsonPath("$.token").isNotEmpty());
	}

	@Test
	void credencialesIncorrectasDevuelven401() throws Exception {
		mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"user\",\"password\":\"incorrecta\"}"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.message").value("Usuario o contraseña incorrectos"));
	}

	@Test
	void loginIncompletoDevuelve400() throws Exception {
		mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"username\":\"\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400));
	}

	@Test
	void recursoProtegidoSinTokenDevuelve401() throws Exception {
		mockMvc.perform(get("/api/recursos"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.message").value("Se requiere un token Bearer válido"));
	}

	@Test
	void tokenInvalidoDevuelve401() throws Exception {
		mockMvc.perform(get("/api/recursos").header(HttpHeaders.AUTHORIZATION, "Bearer token-invalido"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("El token de autenticación no es válido"));
	}

	@Test
	void usuarioAutenticadoAccedeAlRecurso() throws Exception {
		String token = login("user", "password");

		mockMvc.perform(get("/api/recursos").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.usuario").value("user"));
	}

	@Test
	void usuarioSinRolAdminRecibe403() throws Exception {
		String token = login("user", "password");

		mockMvc.perform(get("/api/admin/panel").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.status").value(403))
				.andExpect(jsonPath("$.message").value("No cuenta con permisos suficientes para este recurso"));
	}

	@Test
	void administradorAccedeAlPanel() throws Exception {
		String token = login("admin", "admin123");

		mockMvc.perform(get("/api/admin/panel").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.usuario").value("admin"));
	}

	private String login(String username, String password) throws Exception {
		MvcResult result = mockMvc
				.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
						.content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
				.andExpect(status().isOk())
				.andReturn();
		return JsonPath.read(result.getResponse().getContentAsString(), "$.token");
	}

}
