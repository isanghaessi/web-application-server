package http.type;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum HttpMethod {
	GET("GET"),
	POST("POST");

	private final String method;

	private static final Map<String, HttpMethod> methodMap = Arrays.stream(HttpMethod.values())
		.collect(Collectors.toMap(HttpMethod::getMethod, Function.identity()));

	HttpMethod(String method) {
		this.method = method;
	}

	public static HttpMethod getByMethod(String method) {
		if (!methodMap.containsKey(method)) {
			return null;
		}

		return methodMap.get(method);
	}

	public String getMethod() {
		return method;
	}
}
