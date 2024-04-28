package webserver.model;

import java.util.regex.Pattern;

import http.type.HttpMethod;

public class HandlerKey {
	private final HttpMethod httpMethod;
	private final Pattern pathPattern;

	public HandlerKey(HttpMethod httpMethod, String path) {
		this.httpMethod = httpMethod;
		this.pathPattern = Pattern.compile(path);
	}

	public boolean isAcceptable(HandlerValue handlerValue) {
		return this.httpMethod.equals(handlerValue.getHttpMethod()) && pathPattern.matcher(handlerValue.getPath()).find();
	}
}
