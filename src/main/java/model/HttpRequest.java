package model;

import java.util.Map;

import type.HttpMethod;
import util.HttpRequestUtils;

public class HttpRequest {
	private static final String CONTENT_TYPE = "Content-Type";
	private static final String CONTENT_TYPE_FORM_DATA = "application/x-www-form-urlencoded";

	private HttpMethod httpMethod;
	private String path;
	private Map<String, String> headers;
	private String body;
	private Map<String, String> formData;

	public HttpMethod getHttpMethod() {
		return httpMethod;
	}

	public String getPath() {
		return path;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
		setFormData();
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setHttpMethod(HttpMethod httpMethod) {
		this.httpMethod = httpMethod;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public Map<String, String > getFormData() {
		return this.formData;
	}

	private void setFormData() {
		if (isFormDataRequest()) {
			this.formData = HttpRequestUtils.parseFormDataOrQueryString(body);
		}
	}

	private boolean isFormDataRequest() {
		return headers.containsKey(CONTENT_TYPE) && headers.get(CONTENT_TYPE).equals(CONTENT_TYPE_FORM_DATA);
	}

	@Override
	public String toString() {
		return "HttpRequest{" +
			"httpMethod=" + httpMethod +
			", path='" + path + '\'' +
			", headers=" + headers +
			", body='" + body + '\'' +
			", formData=" + formData +
			'}';
	}
}
