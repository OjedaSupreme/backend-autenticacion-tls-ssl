package com.practica.u3.security.web;

import com.practica.u3.security.security.SecurityAudit;
import com.practica.u3.security.web.dto.ApiErrorResponse;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiErrorController implements ErrorController {

	@RequestMapping("/error")
	public ResponseEntity<ApiErrorResponse> error(HttpServletRequest request) {
		int status = statusOf(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE));
		String path = stringOf(request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI));
		if (path == null || path.isBlank()) {
			path = request.getRequestURI();
		}
		Throwable failure = throwableOf(request.getAttribute(RequestDispatcher.ERROR_EXCEPTION));
		String detail = failure == null ? stringOf(request.getAttribute(RequestDispatcher.ERROR_MESSAGE))
				: failure.toString();
		SecurityAudit.warn("REQUEST_ERROR", "status=" + status + " path=" + path + " detalle=" + detail);
		HttpStatus httpStatus = HttpStatus.resolve(status);
		if (httpStatus == null) {
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			status = httpStatus.value();
		}
		return ResponseEntity.status(httpStatus)
				.body(ApiErrorResponse.of(status, httpStatus.getReasonPhrase(), messageFor(status, detail), path));
	}

	private int statusOf(Object value) {
		if (value instanceof Integer status) {
			return status;
		}
		return HttpStatus.INTERNAL_SERVER_ERROR.value();
	}

	private String stringOf(Object value) {
		return value == null ? "" : String.valueOf(value);
	}

	private Throwable throwableOf(Object value) {
		return value instanceof Throwable throwable ? throwable : null;
	}

	private String messageFor(int status, String detail) {
		String normalized = detail == null ? "" : detail.toLowerCase();
		if (normalized.contains("handshake") || normalized.contains("ssl") || normalized.contains("tls")) {
			return "No se pudo completar el handshake TLS. Use TLS 1.2 o TLS 1.3.";
		}
		if (status == HttpStatus.UNAUTHORIZED.value()) {
			return "Se requiere un token Bearer válido";
		}
		if (status == HttpStatus.FORBIDDEN.value()) {
			return "No cuenta con permisos suficientes para este recurso";
		}
		if (status == HttpStatus.NOT_FOUND.value()) {
			return "El recurso solicitado no existe";
		}
		return "No se pudo procesar la petición";
	}

}
