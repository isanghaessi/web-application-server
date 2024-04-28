package webserver.model;

import http.type.HttpMethod;

public class HandlerValue {
	private final HttpMethod httpMethod;
	private final String path;

	public HandlerValue(HttpMethod httpMethod, String path) {
		this.httpMethod = httpMethod;
		this.path = path;
	}

	public HttpMethod getHttpMethod() {
		return this.httpMethod;
	}

	public String getPath() {
		return this.path;
	}
}
