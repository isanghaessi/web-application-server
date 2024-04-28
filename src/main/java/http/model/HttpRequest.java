package http.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import http.type.HttpMethod;
import http.util.HttpUtils;

public class HttpRequest {
	private static final String CONTENT_TYPE = "Content-Type";
	public static final String COOKIE = "Cookie";
	private static final String CONTENT_TYPE_FORM_DATA = "application/x-www-form-urlencoded";

	private static final String LOG_IN_COOKIE_KEY = "logined";
	private static final String LOGGED_IN_COOKIE_VALUE = "true";

	private static final String COOKIE_DELIMITER = ";";
	private static final String COOKIE_EQUAL = "=";

	private static final int COOKIE_TOKEN_LENGTH = 2;
	private static final int COOKIE_TOKEN_KEY_INDEX = 0;
	private static final int COOKIE_TOKEN_VALUE_INDEX = 1;

	private HttpMethod httpMethod;
	private String path;
	private Map<String, String> headers;
	private Map<String, String> cookies;
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
		_setCookies();
	}

	public Map<String, String> getCookies() {
		return this.cookies;
	}

	public Map<String, String> getFormData() {
		return this.formData;
	}

	public boolean isLoggedIn() {
		if (!cookies.containsKey(LOG_IN_COOKIE_KEY)) {
			return false;
		}

		String logInCookieValue = cookies.get(LOG_IN_COOKIE_KEY);

		return logInCookieValue.equals(LOGGED_IN_COOKIE_VALUE);
	}

	private void setFormData() {
		if (isFormDataRequest()) {
			this.formData = HttpUtils.parseFormDataOrQueryString(body);
		}
	}

	private boolean isFormDataRequest() {
		return headers.containsKey(CONTENT_TYPE) && headers.get(CONTENT_TYPE).equals(CONTENT_TYPE_FORM_DATA);
	}

	private void _setCookies() {
		String cookieString = headers.get(COOKIE);
		if (Objects.isNull(cookieString) || cookieString.length() < 1) {
			return;
		}

		Map<String, String> cookies = new HashMap<>();
		for (String cookieTokenString : cookieString.split(COOKIE_DELIMITER)) {
			String[] cookieToken = cookieTokenString.split(COOKIE_EQUAL);
			if (cookieToken.length != COOKIE_TOKEN_LENGTH) {
				continue;
			}

			cookies.put(cookieToken[COOKIE_TOKEN_KEY_INDEX], cookieToken[COOKIE_TOKEN_VALUE_INDEX]);
		}

		this.cookies = cookies;
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
