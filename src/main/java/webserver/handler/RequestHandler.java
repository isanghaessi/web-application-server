package webserver.handler;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import type.HttpRequest;
import webserver.handler.type.HandlerMapping;
import type.HttpMethod;
import type.HttpStatus;
import util.HttpRequestUtils;
import webserver.handler.model.HandlerKey;

public class RequestHandler extends Thread {
	private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

	private Socket connection;

	public RequestHandler(Socket connectionSocket) {
		this.connection = connectionSocket;
	}

	public void run() {
		log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(), connection.getPort());

		try (InputStream in = connection.getInputStream(); DataOutputStream dos = new DataOutputStream(connection.getOutputStream())) {
			response(in, dos);
		} catch (IOException ioException) {
			log.error(String.format("RequestHandler.run - 스트림을 열고 닫는데 문제가 생겼습니다."), ioException);
		}
	}

	private void response(InputStream in, DataOutputStream dos) throws IOException {
		try {
			HttpRequest httpRequest = HttpRequestUtils.parse(in);
			HttpMethod httpMethod = httpRequest.getHttpMethod();
			String path = httpRequest.getPath();

			if (HttpRequestUtils.isFileRequest(httpMethod, path)) {
				processFileRequest(dos, path);
			}
			processNotFileRequest(httpRequest, dos, httpMethod, path);
		} catch (IllegalArgumentException illegalArgumentException) {
			log.error(String.format("%s %d", HttpStatus.BAD_REQUEST.name(), HttpStatus.BAD_REQUEST.getCode()), illegalArgumentException);

			HttpRequestUtils.responseHeader(dos, HttpStatus.BAD_REQUEST);
		} catch (IllegalAccessException illegalAccessException) {
			log.error(String.format("%s %d", HttpStatus.NOT_ALLOWED_METHOD.name(), HttpStatus.NOT_ALLOWED_METHOD.getCode()), illegalAccessException);

			HttpRequestUtils.responseHeader(dos, HttpStatus.NOT_ALLOWED_METHOD);
		} catch (IOException ioException) {
			log.error(String.format("%s %d", HttpStatus.SERVER_ERROR.name(), HttpStatus.SERVER_ERROR.getCode()), ioException);

			HttpRequestUtils.responseHeader(dos, HttpStatus.SERVER_ERROR);
		} finally {
			dos.flush();
		}
	}

	private void processFileRequest(DataOutputStream dos, String path) throws IOException {
		try {
			byte[] body = HttpRequestUtils.getFile(path);
			HttpRequestUtils.response200Header(dos, body.length);
			HttpRequestUtils.responseBody(dos, body);

			log.info(String.format("response colmplete! - path: {%s}", path));
		} catch (IllegalArgumentException illegalArgumentException) {
			log.error(String.format("%s %d - path: {%s}", HttpStatus.NOT_FOUND.name(), HttpStatus.NOT_FOUND.getCode(), path), illegalArgumentException);

			HttpRequestUtils.responseHeader(dos, HttpStatus.NOT_ALLOWED_METHOD);
		}
	}

	private static void processNotFileRequest(HttpRequest httpRequest, DataOutputStream dos, HttpMethod httpMethod, String path) throws IOException {
		HandlerKey handlerKey = new HandlerKey(httpMethod, path);
		HandlerMapping.getByHandlerKey(handlerKey).handle(httpRequest, dos);
	}
}
