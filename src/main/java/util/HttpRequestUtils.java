package util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import model.HttpRequest;
import model.Pair;
import type.HttpMethod;
import type.HttpStatus;
import webserver.handler.RequestHandler;

public class HttpRequestUtils {
	private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

	public static final String DEFAULT_PATH = "/index.html";

	private static final String WEBAPP_PATH = "./webapp";
	private static final String NO_FILE_NAME = "/";
	private static final String HEADER_EQUAL = ":";
	private static final String HEADER_DELIMITER = "\r\n";
	private static final String QUERY_STRING_OR_FORM_DATA_DELIMITER = "&";
	private static final String QUERY_STRING_OR_FORM_DATA_EQUAL = "=";
	private static final String COOKIE_DELIMITER = ";";

	private static final Pattern REQUEST_BODY_PATTERN = Pattern.compile("\r\n\r\n(.*)$");

	private static final int HTTP_METHOD_INDEX = 0;
	private static final int PATH_INDEX = 1;
	private static final int REQUEST_BODY_PATTERN_GROUP_INDEX = 1;

	private HttpRequestUtils() {
	}

	/**
	 * @param queryString은 URL에서 ? 이후에 전달되는 field1=value1&field2=value2 형식임
	 * @return
	 */
	public static Map<String, String> parseQueryString(String queryString) {
		return parseValues(queryString, QUERY_STRING_OR_FORM_DATA_DELIMITER);
	}

	/**
	 * @param 쿠키 값은 name1=value1; name2=value2 형식임
	 * @return
	 */
	public static Map<String, String> parseCookies(String cookies) {
		return parseValues(cookies, COOKIE_DELIMITER);
	}

	private static Map<String, String> parseValues(String values, String separator) {
		if (Strings.isNullOrEmpty(values)) {
			return Maps.newHashMap();
		}

		String[] tokens = values.split(separator);
		return Arrays.stream(tokens)
			.map(token -> getKeyValue(token, QUERY_STRING_OR_FORM_DATA_EQUAL))
			.filter(Objects::nonNull)
			.collect(Collectors.toMap(pair -> DecodeUtils.decodeURI(pair.getKey()), pair -> DecodeUtils.decodeURI(pair.getValue())));
	}

	public static Map<String, String> parseFormDataOrQueryString(String valueString) {
		return parseValues(valueString, QUERY_STRING_OR_FORM_DATA_DELIMITER);
	}

	public static HttpRequest parse(InputStream inputStream) throws IOException, IllegalAccessException {
		HttpRequest httpRequest = new HttpRequest();

		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		String firstLine = bufferedReader.readLine();

		if (Objects.isNull(firstLine) || firstLine.isEmpty()) {
			return httpRequest;
		}

		String[] tokens = firstLine.split(" ");
		if (tokens.length < 2) {
			throw new IllegalArgumentException(String.format("RequestHandler.parse - 요청의 헤더 첫줄이 이상합니다. firstLine: {%s}", firstLine));
		}

		String httpRequestMethod = tokens[HTTP_METHOD_INDEX];
		if (HttpMethod.getByMethod(httpRequestMethod) == null) {
			throw new IllegalAccessException(String.format("RequestHandler.parse - 지원하지 않는 메소드입니다. method: {%s}", httpRequestMethod));
		}
		HttpMethod httpMethod = HttpMethod.getByMethod(httpRequestMethod);
		if (Objects.isNull(httpMethod)) {
			throw new IllegalArgumentException(String.format("RequestHandler.parse - 요청의 헤더 첫줄이 이상합니다. firstLine: {%s}", firstLine));
		}
		httpRequest.setHttpMethod(httpMethod);

		String path = tokens[PATH_INDEX];
		if (Objects.isNull(path) || path.isEmpty() || NO_FILE_NAME.equals(path)) {
			httpRequest.setPath(DEFAULT_PATH);
		} else {
			httpRequest.setPath(path);
		}

		StringBuilder stringBuilder = new StringBuilder();
		while (bufferedReader.ready()) {
			int currentCharacter = bufferedReader.read();
			stringBuilder.append((char)currentCharacter);
		}

		String restRequest = stringBuilder.toString();

		String headerString = restRequest;
		String body = "";
		Matcher bodyMatcher = REQUEST_BODY_PATTERN.matcher(restRequest);
		if (bodyMatcher.find()) {
			body = bodyMatcher.group(REQUEST_BODY_PATTERN_GROUP_INDEX);
		}
		httpRequest.setHeaders(parseHeaders(headerString.replace(body, "")));
		httpRequest.setBody(body);

		return httpRequest;
	}

	public static byte[] getFile(String fileName) throws IOException {
		File file = new File(WEBAPP_PATH + fileName);

		if (!file.exists() || !file.canRead() || file.isDirectory()) {
			throw new IllegalArgumentException(String.format("RequestHandler.getFile - 해당 파일이 존재하지 않거나 접근할 수 없습니다. fileName: {%s}", fileName));
		}

		return Files.readAllBytes(file.toPath());
	}

	public static void responseHeader(DataOutputStream dataOutputStream, HttpStatus httpStatus, Pair... cookies) throws IOException {
		dataOutputStream.writeBytes(String.format("HTTP/1.1 %d %s \r\n", httpStatus.getCode(), httpStatus.getMessage()));
		for (Pair cookie : cookies) {
			dataOutputStream.writeBytes(String.format("Set-Cookie: %s=%s \r\n", cookie.getKey(), cookie.getValue()));
		}
		dataOutputStream.writeBytes("\r\n");
	}

	public static void response200HtmlHeader(DataOutputStream dataOutputStream, int lengthOfBodyContent, Pair... cookies) throws IOException {
		dataOutputStream.writeBytes("HTTP/1.1 200 OK \r\n");
		dataOutputStream.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
		dataOutputStream.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
		for (Pair cookie : cookies) {
			dataOutputStream.writeBytes(String.format("Set-Cookie: %s=%s \r\n", cookie.getKey(), cookie.getValue()));
		}
		dataOutputStream.writeBytes("\r\n");
	}

	public static void response200CssHeader(DataOutputStream dataOutputStream, int lengthOfBodyContent, Pair... cookies) throws IOException {
		dataOutputStream.writeBytes("HTTP/1.1 200 OK \r\n");
		dataOutputStream.writeBytes("Content-Type: text/css;charset=utf-8\r\n");
		dataOutputStream.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
		for (Pair cookie : cookies) {
			dataOutputStream.writeBytes(String.format("Set-Cookie: %s=%s \r\n", cookie.getKey(), cookie.getValue()));
		}
		dataOutputStream.writeBytes("\r\n");
	}

	public static void redirect(DataOutputStream dataOutputStream, Pair... cookies) throws IOException {
		dataOutputStream.writeBytes(String.format("HTTP/1.1 %d %s \r\n", HttpStatus.REDIRECT.getCode(), HttpStatus.REDIRECT.getMessage()));
		dataOutputStream.writeBytes(String.format("Location: %s \r\n", DEFAULT_PATH));
		for (Pair cookie : cookies) {
			dataOutputStream.writeBytes(String.format("Set-Cookie: %s=%s \r\n", cookie.getKey(), cookie.getValue()));
		}
		dataOutputStream.writeBytes("\r\n");
	}

	public static void redirect(DataOutputStream dataOutputStream, String path, Pair... cookies) throws IOException {
		dataOutputStream.writeBytes(String.format("HTTP/1.1 %d %s \r\n", HttpStatus.REDIRECT.getCode(), HttpStatus.REDIRECT.getMessage()));
		dataOutputStream.writeBytes(String.format("Location: %s \r\n", path));
		for (Pair cookie : cookies) {
			dataOutputStream.writeBytes(String.format("Set-Cookie: %s=%s", cookie.getKey(), cookie.getValue()));
		}
		dataOutputStream.writeBytes("\r\n");
	}

	public static void responseBody(DataOutputStream dataOutputStream, byte[] body) throws IOException {
		dataOutputStream.write(body, 0, body.length);
	}

	private static Map<String, String> parseHeaders(String headersString) {
		Map<String, String> headers = new HashMap<>();

		String[] fullHeaders = headersString.split(HEADER_DELIMITER);
		for (String fullHeader : fullHeaders) {
			int headerEqualIndex = fullHeader.indexOf(HEADER_EQUAL);
			String key = DecodeUtils.decodeURI(fullHeader.substring(0, headerEqualIndex).trim());
			String value = DecodeUtils.decodeURI(fullHeader.substring(headerEqualIndex + 1).trim());
			headers.put(key, value);
		}

		return headers;
	}

	private static Pair getKeyValue(String keyValue, String regex) {
		if (Strings.isNullOrEmpty(keyValue)) {
			return null;
		}

		String[] tokens = keyValue.split(regex);
		if (tokens.length != 2) {
			return null;
		}

		return new Pair(tokens[0], tokens[1]);
	}
}
