package webserver.handler;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Objects;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import http.model.HttpRequest;
import http.model.HttpResponse;
import http.type.HttpMethod;
import http.type.HttpStatus;
import http.util.HttpUtils;
import webserver.model.HandlerValue;
import webserver.type.HandlerMapping;

public class RequestHandler extends Thread {
	private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
	private static final Pattern CSS_FILE_PATTERN = Pattern.compile(".css$");

	private Socket connection;

	public RequestHandler(Socket connectionSocket) {
		this.connection = connectionSocket;
	}

	@Override
	public void run() {
		log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(), connection.getPort());

		try (InputStream inputStream = connection.getInputStream();
			 DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream())) {
			response(inputStream, dataOutputStream);
		} catch (IOException ioException) {
			log.error(String.format("RequestHandler.run - 스트림을 열고 닫는데 문제가 생겼습니다."), ioException);
		}
	}

	private void response(InputStream inputStream, DataOutputStream dataOutputStream) throws IOException {
		try {
			HttpRequest httpRequest = HttpUtils.parse(inputStream);
			HttpMethod httpMethod = httpRequest.getHttpMethod();
			String path = httpRequest.getPath();

			if (!processNotFileRequest(httpRequest, dataOutputStream, httpMethod, path)) {
				processFileRequest(dataOutputStream, path);
			}
		} catch (IllegalArgumentException illegalArgumentException) {
			log.error(String.format("%s %d", HttpStatus.BAD_REQUEST.name(), HttpStatus.BAD_REQUEST.getCode()), illegalArgumentException);

			HttpResponse.makeBadRequest()
				.response(dataOutputStream);
		} catch (IllegalAccessException illegalAccessException) {
			log.error(String.format("%s %d", HttpStatus.NOT_ALLOWED_METHOD.name(), HttpStatus.NOT_ALLOWED_METHOD.getCode()), illegalAccessException);

			HttpResponse.makeNotAllowedMethod()
				.response(dataOutputStream);
		} catch (Exception exception) {
			log.error(String.format("%s %d", HttpStatus.SERVER_ERROR.name(), HttpStatus.SERVER_ERROR.getCode()), exception);

			HttpResponse.makeServerError()
				.response(dataOutputStream);
		} finally {
			dataOutputStream.flush();
		}
	}

	private void processFileRequest(DataOutputStream dataOutputStream, String path) throws IOException {
		try {
			byte[] body = HttpUtils.getFile(path);

			if (isCssRequest(path)) {
				HttpResponse.makeCssHttpResponse(body)
					.response(dataOutputStream);
			} else {
				HttpResponse.makeHtmlHttpResponse(body)
					.response(dataOutputStream);
			}

			log.info(String.format("response colmplete! - path: {%s}", path));
		} catch (IllegalArgumentException illegalArgumentException) {
			log.error(String.format("%s %d - path: {%s}", HttpStatus.NOT_FOUND.name(), HttpStatus.NOT_FOUND.getCode(), path), illegalArgumentException);

			HttpResponse.makeNotAllowedMethod()
				.response(dataOutputStream);
		}
	}

	private static boolean processNotFileRequest(HttpRequest httpRequest, DataOutputStream dataOutputStream, HttpMethod httpMethod, String path) throws IOException {
		HandlerValue handlerValue = new HandlerValue(httpMethod, path);
		HandlerMapping handlerMapping = HandlerMapping.getByHandlerKey(handlerValue);
		if (Objects.isNull(handlerMapping)) {
			log.debug(String.format("RequestHandler.processNotFileRequest - 지원하는 handlerMapping이 없습니다. httpRequest: {%s}", httpRequest));
			return false;
		}

		handlerMapping.handle(httpRequest, dataOutputStream);

		return true;
	}

	private boolean isCssRequest(String path) {
		return CSS_FILE_PATTERN.matcher(path).find();
	}
}
