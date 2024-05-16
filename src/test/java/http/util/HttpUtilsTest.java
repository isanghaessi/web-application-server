package http.util;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import org.junit.Test;

import common.model.Pair;
import http.model.HttpRequest;
import http.util.HttpUtils;

public class HttpUtilsTest {
	@Test
	public void parseQueryString() {
		String queryString = "userId=javajigi";
		Map<String, String> parameters = HttpUtils.parseQueryString(queryString);
		assertThat(parameters.get("userId"), is("javajigi"));
		assertThat(parameters.get("password"), is(nullValue()));

		queryString = "userId=javajigi&password=password2";
		parameters = HttpUtils.parseQueryString(queryString);
		assertThat(parameters.get("userId"), is("javajigi"));
		assertThat(parameters.get("password"), is("password2"));
	}

	@Test
	public void parseQueryString_null() {
		Map<String, String> parameters = HttpUtils.parseQueryString(null);
		assertThat(parameters.isEmpty(), is(true));

		parameters = HttpUtils.parseQueryString("");
		assertThat(parameters.isEmpty(), is(true));

		parameters = HttpUtils.parseQueryString(" ");
		assertThat(parameters.isEmpty(), is(true));
	}

	@Test
	public void parseQueryString_invalid() {
		String queryString = "userId=javajigi&password";
		Map<String, String> parameters = HttpUtils.parseQueryString(queryString);
		assertThat(parameters.get("userId"), is("javajigi"));
		assertThat(parameters.get("password"), is(nullValue()));
	}

	@Test
	public void parseCookies() {
		String cookies = "logined=true; JSessionId=1234";
		Map<String, String> parameters = HttpUtils.parseCookies(cookies);
		assertThat(parameters.get("logined"), is("true"));
		assertThat(parameters.get("JSessionId"), is("1234"));
		assertThat(parameters.get("session"), is(nullValue()));
	}

	@Test
	public void parseFormDataOrQueryString() {
		// given
		String dataOrQueryString = "logined=true&JSessionId=1234";

		// when
		Map<String, String> parameters = HttpUtils.parseFormDataOrQueryString(dataOrQueryString);

		// then
		assertThat(parameters.get("logined"), is("true"));
		assertThat(parameters.get("JSessionId"), is("1234"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void parseWhenWrongHeader() throws IOException, IllegalAccessException {
		// given
		String httpRequest = "GET\r\n"
			+ "User-Agent: Mozilla/4.0 (compatible; MSIE5.01; Windows NT)\r\n"
			+ "Host: www.tutorialspoint.com\r\n"
			+ "Accept-Language: en-us\r\n"
			+ "Accept-Encoding: gzip, deflate\r\n"
			+ "Connection: Keep-Alive";

		// when
		// then
		HttpUtils.parse(new ByteArrayInputStream(httpRequest.getBytes()));
	}

	@Test(expected = IllegalAccessException.class)
	public void parseWhenWrongMethod() throws IOException, IllegalAccessException {
		// given
		String httpRequest = "WRONG /hello.htm HTTP/1.1\r\n"
			+ "User-Agent: Mozilla/4.0 (compatible; MSIE5.01; Windows NT)\r\n"
			+ "Host: www.tutorialspoint.com\r\n"
			+ "Accept-Language: en-us\r\n"
			+ "Accept-Encoding: gzip, deflate\r\n"
			+ "Connection: Keep-Alive";

		// when
		// then
		HttpUtils.parse(new ByteArrayInputStream(httpRequest.getBytes()));
	}

	@Test
	public void parseWhenDefaultPath() throws IOException, IllegalAccessException {
		// given
		String httpRequestString = "GET / HTTP/1.1\r\n"
			+ "User-Agent: Mozilla/4.0 (compatible; MSIE5.01; Windows NT)\r\n"
			+ "Host: www.tutorialspoint.com\r\n"
			+ "Accept-Language: en-us\r\n"
			+ "Accept-Encoding: gzip, deflate\r\n"
			+ "Connection: Keep-Alive\r\n"
			+ "\r\n"
			+ "body";

		// when
		HttpRequest httpRequest = HttpUtils.parse(new ByteArrayInputStream(httpRequestString.getBytes()));

		// then
		assertThat(httpRequest.getPath(), is("/index.html"));
		
		Map<String, String> headers = httpRequest.getHeaders();
		assertThat(headers.size(), is(5));
		assertThat(headers.get("User-Agent"), is("Mozilla/4.0 (compatible; MSIE5.01; Windows NT)"));
		assertThat(headers.get("Host"), is("www.tutorialspoint.com"));
		assertThat(headers.get("Accept-Language"), is("en-us"));
		assertThat(headers.get("Accept-Encoding"), is("gzip, deflate"));
		assertThat(headers.get("Connection"), is("Keep-Alive"));

		assertThat(httpRequest.getBody(), is("body"));
	}

	@Test
	public void parse() throws IOException, IllegalAccessException {
		// given
		String httpRequestString = "GET /path HTTP/1.1\r\n"
			+ "User-Agent: Mozilla/4.0 (compatible; MSIE5.01; Windows NT)\r\n"
			+ "Host: www.tutorialspoint.com\r\n"
			+ "Accept-Language: en-us\r\n"
			+ "Accept-Encoding: gzip, deflate\r\n"
			+ "Connection: Keep-Alive\r\n"
			+ "\r\n"
			+ "body";

		// when
		HttpRequest httpRequest = HttpUtils.parse(new ByteArrayInputStream(httpRequestString.getBytes()));

		// then
		assertThat(httpRequest.getPath(), is("/path"));

		Map<String, String> headers = httpRequest.getHeaders();
		assertThat(headers.size(), is(5));
		assertThat(headers.get("User-Agent"), is("Mozilla/4.0 (compatible; MSIE5.01; Windows NT)"));
		assertThat(headers.get("Host"), is("www.tutorialspoint.com"));
		assertThat(headers.get("Accept-Language"), is("en-us"));
		assertThat(headers.get("Accept-Encoding"), is("gzip, deflate"));
		assertThat(headers.get("Connection"), is("Keep-Alive"));

		assertThat(httpRequest.getBody(), is("body"));
	}
}
