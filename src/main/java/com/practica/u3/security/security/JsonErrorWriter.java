package com.practica.u3.security.security;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.springframework.http.MediaType;

public final class JsonErrorWriter {

	private JsonErrorWriter() {
	}

	public static void write(HttpServletResponse response, int status, String error, String message, String path)
			throws IOException {
		if (response.isCommitted()) {
			return;
		}
		response.setStatus(status);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setHeader("Cache-Control", "no-store");
		String body = """
				{"timestamp":"%s","status":%d,"error":"%s","message":"%s","path":"%s"}
				""".formatted(Instant.now(), status, escape(error), escape(message), escape(path));
		response.getWriter().write(body);
	}

	private static String escape(String value) {
		if (value == null) {
			return "";
		}
		return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", " ").replace("\n", " ");
	}

}
