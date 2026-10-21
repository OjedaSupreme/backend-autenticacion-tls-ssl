package com.practica.u3.security.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SecurityAudit {

	private static final Logger log = LoggerFactory.getLogger("SECURITY_AUDIT");

	private SecurityAudit() {
	}

	public static void info(String event, String detail) {
		log.info("event={} {}", event, detail);
	}

	public static void warn(String event, String detail) {
		log.warn("event={} {}", event, detail);
	}

	public static void debug(String event, String detail) {
		log.debug("event={} {}", event, detail);
	}

}
