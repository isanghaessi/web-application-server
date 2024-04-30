package http.model;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import common.model.Pair;
import http.type.HttpStatus;

public class HttpResponse {
	private static final String HTTP = "HTTP/1.1";

	public static final String CONTENT_TYPE_KEY = "Content-Type";
	public static final String CONTENT_LENGTH_KEY = "Content-Length";

	public static final String CONTENT_TYPE_VALUE_TEXT_CSS = "text/css;charset=utf-8";
	public static final String CONTENT_TYPE_VALUE_TEXT_HTML = "text/html;charset=utf-8";

	private HttpStatus httpStatus;
	private Map<String, String> headers = new HashMap<>();
	private Map<String, String> cookies = new HashMap<>();
	private byte[] body;

	public void setHttpStatus(HttpStatus httpStatus) {
		this.httpStatus = httpStatus;
	}

	public void addHeader(String key, String value) {
		headers.put(key, value);
	}

	public void addCookie(String key, String value) {
		cookies.put(key, value);
	}

	public void setBody(String body) {
		this.body = body.getBytes();

		addHeader(CONTENT_LENGTH_KEY, String.valueOf(body.length()));
	}

	public void setBody(byte[] body) {
		this.body = body;

		addHeader(CONTENT_LENGTH_KEY, String.valueOf(body.length));
	}

	private HttpResponse() {
	}

	public static HttpResponse makeEmpty() {
		return new HttpResponse();
	}

	public static HttpResponse makeBadRequest() {
		HttpResponse httpResponse = new HttpResponse();
		httpResponse.setHttpStatus(HttpStatus.BAD_REQUEST);

		return httpResponse;
	}

	public static HttpResponse makeNotAllowedMethod() {
		HttpResponse httpResponse = new HttpResponse();
		httpResponse.setHttpStatus(HttpStatus.NOT_ALLOWED_METHOD);

		return httpResponse;
	}

	public static HttpResponse makeServerError() {
		HttpResponse httpResponse = new HttpResponse();
		httpResponse.setHttpStatus(HttpStatus.SERVER_ERROR);

		return httpResponse;
	}

	public static HttpResponse makeHtmlHttpResponse(byte[] body, Pair... cookies) {
		HttpResponse httpResponse = new HttpResponse();
		httpResponse.setHttpStatus(HttpStatus.OK);
		httpResponse.addHeader(CONTENT_TYPE_KEY, CONTENT_TYPE_VALUE_TEXT_HTML);
		httpResponse.setBody(body);

		for (Pair cookie : cookies) {
			httpResponse.addCookie(cookie.getKey(), cookie.getValue());
		}

		return httpResponse;
	}

	public static HttpResponse makeHtmlHttpResponse(String body, Pair... cookies) {
		return makeHtmlHttpResponse(body.getBytes(StandardCharsets.UTF_8), cookies);
	}

	public static HttpResponse makeCssHttpResponse(byte[] body, Pair... cookies) {
		HttpResponse httpResponse = new HttpResponse();
		httpResponse.setHttpStatus(HttpStatus.OK);
		httpResponse.addHeader(CONTENT_TYPE_KEY, CONTENT_TYPE_VALUE_TEXT_CSS);
		httpResponse.setBody(body);

		for (Pair cookie : cookies) {
			httpResponse.addCookie(cookie.getKey(), cookie.getValue());
		}

		return httpResponse;
	}

	public void response(DataOutputStream dataOutputStream, Pair... cookies) throws IOException {
		for (Pair cookie : cookies) {
			addCookie(cookie.getKey(), cookie.getValue());
		}

		dataOutputStream.writeBytes(String.format("%s %d %s\r\n", HTTP, httpStatus.getCode(), httpStatus.getMessage()));
		for (Map.Entry<String, String> header : headers.entrySet()) {
			dataOutputStream.writeBytes(String.format("%s: %s\r\n", header.getKey(), header.getValue()));
		}
		for (Map.Entry<String, String> cookie : headers.entrySet()) {
			dataOutputStream.writeBytes(String.format("Set-Cookie: %s=%s \r\n", cookie.getKey(), cookie.getValue()));
		}
		dataOutputStream.writeBytes("\r\n");

		dataOutputStream.write(body, 0, body.length);
	}

	public void redirect(DataOutputStream dataOutputStream, String path, Pair... cookies) throws IOException {
		for (Pair cookie : cookies) {
			addCookie(cookie.getKey(), cookie.getValue());
		}

		dataOutputStream.writeBytes(String.format("%s %d %s\r\n", HTTP, HttpStatus.REDIRECT.getCode(), HttpStatus.REDIRECT.getMessage()));
		dataOutputStream.writeBytes(String.format("Location: %s\r\n", path));
		for (Map.Entry<String, String> cookie : headers.entrySet()) {
			dataOutputStream.writeBytes(String.format("Set-Cookie: %s=%s \r\n", cookie.getKey(), cookie.getValue()));
		}
		dataOutputStream.writeBytes("\r\n");
	}
}
