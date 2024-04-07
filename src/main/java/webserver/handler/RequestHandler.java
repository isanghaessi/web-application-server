package webserver.handler;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.HttpRequest;
import type.HttpMethod;
import type.HttpStatus;
import util.HttpRequestUtils;
import webserver.handler.model.HandlerKey;
import webserver.handler.model.HandlerValue;
import webserver.handler.type.HandlerMapping;

public class RequestHandler extends Thread {
	private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

	private Socket connection;

	public RequestHandler(Socket connectionSocket) {
		this.connection = connectionSocket;
	}

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
			HttpRequest httpRequest = HttpRequestUtils.parse(inputStream);
			HttpMethod httpMethod = httpRequest.getHttpMethod();
			String path = httpRequest.getPath();

			if (HttpRequestUtils.isFileRequest(httpMethod, path)) {
				processFileRequest(dataOutputStream, path);
			} else {
				processNotFileRequest(httpRequest, dataOutputStream, httpMethod, path);
			}
		} catch (IllegalArgumentException illegalArgumentException) {
			log.error(String.format("%s %d", HttpStatus.BAD_REQUEST.name(), HttpStatus.BAD_REQUEST.getCode()), illegalArgumentException);

			HttpRequestUtils.responseHeader(dataOutputStream, HttpStatus.BAD_REQUEST);
		} catch (IllegalAccessException illegalAccessException) {
			log.error(String.format("%s %d", HttpStatus.NOT_ALLOWED_METHOD.name(), HttpStatus.NOT_ALLOWED_METHOD.getCode()), illegalAccessException);

			HttpRequestUtils.responseHeader(dataOutputStream, HttpStatus.NOT_ALLOWED_METHOD);
		} catch (Exception exception) {
			log.error(String.format("%s %d", HttpStatus.SERVER_ERROR.name(), HttpStatus.SERVER_ERROR.getCode()), exception);

			HttpRequestUtils.responseHeader(dataOutputStream, HttpStatus.SERVER_ERROR);
		} finally {
			dataOutputStream.flush();
		}
	}

	private void processFileRequest(DataOutputStream dataOutputStream, String path) throws IOException {
		try {
			byte[] body = HttpRequestUtils.getFile(path);
			HttpRequestUtils.response200Header(dataOutputStream, body.length);
			HttpRequestUtils.responseBody(dataOutputStream, body);

			log.info(String.format("response colmplete! - path: {%s}", path));
		} catch (IllegalArgumentException illegalArgumentException) {
			log.error(String.format("%s %d - path: {%s}", HttpStatus.NOT_FOUND.name(), HttpStatus.NOT_FOUND.getCode(), path), illegalArgumentException);

			HttpRequestUtils.responseHeader(dataOutputStream, HttpStatus.NOT_ALLOWED_METHOD);
		}
	}

	private static void processNotFileRequest(HttpRequest httpRequest, DataOutputStream dataOutputStream, HttpMethod httpMethod, String path) throws IllegalAccessException, IOException {
		HandlerValue handlerValue = new HandlerValue(httpMethod, path);
		HandlerMapping handlerMapping =  HandlerMapping.getByHandlerKey(handlerValue);
		if (Objects.isNull(handlerMapping)) {
			throw new IllegalAccessException(String.format("RequestHandler.processNotFileRequest - 지원하는 handlerMapping이 없습니다. httpRequest: {%s}", httpRequest));
		}

		handlerMapping.handle(httpRequest, dataOutputStream);
	}
}
