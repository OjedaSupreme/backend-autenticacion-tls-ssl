package com.practica.u3.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = { "server.ssl.enabled=true", "server.port=8443" })
class HttpsRedirectTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void httpSinCifrarSeRedirigeAHttps() throws Exception {
		mockMvc.perform(get("/api/public/estado").secure(false))
				.andExpect(status().isFound())
				.andExpect(header().string("Location", "https://localhost:8443/api/public/estado"))
				.andExpect(content().string(org.hamcrest.Matchers.containsString("TLS 1.2 o TLS 1.3")));
	}

	@Test
	void httpsNoSeRedirige() throws Exception {
		mockMvc.perform(get("/api/public/estado").secure(true)).andExpect(status().isOk());
	}

}
