package webserver.handler.model;

import type.HttpMethod;

public class HandlerKey {
	private final HttpMethod httpMethod;
	private final String path;

	public HandlerKey(HttpMethod httpMethod, String path) {
		this.httpMethod = httpMethod;
		this.path = path;
	}
}
