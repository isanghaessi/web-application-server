package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import type.HttpStatus;

public class RequestHandler extends Thread {
	private static final String WEBAPP_PATH = "./webapp";
	private static final String DEFAULT_FILE_NAME = "/index.html";
	private static final String NO_FILE_NAME = "/";
	private static final String GET = "GET";
	private static final int HTTP_METHOD_INDEX = 0;
	private static final int PATH_INDEX = 1;

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
			String fileName = getFileName(in);

			try {
				byte[] body = getFile(fileName);
				response200Header(dos, body.length);
				responseBody(dos, body);

				log.info(String.format("response colmplete! - fileName: {%s}", fileName));
			} catch (IllegalArgumentException illegalArgumentException) {
				log.error(String.format("%s %d - fileName: {%s}", HttpStatus.NOT_FOUND.name(), HttpStatus.NOT_FOUND.getCode(), fileName), illegalArgumentException);

				responseHeader(dos, HttpStatus.NOT_ALLOWED_METHOD);
			}
		} catch (IllegalArgumentException illegalArgumentException) {
			log.error(String.format("%s %d", HttpStatus.BAD_REQUEST.name(), HttpStatus.BAD_REQUEST.getCode()), illegalArgumentException);

			responseHeader(dos, HttpStatus.BAD_REQUEST);
		} catch (IllegalAccessException illegalAccessException) {
			log.error(String.format("%s %d", HttpStatus.NOT_ALLOWED_METHOD.name(), HttpStatus.NOT_ALLOWED_METHOD.getCode()), illegalAccessException);

			responseHeader(dos, HttpStatus.NOT_ALLOWED_METHOD);
		} catch (IOException ioException) {
			log.error(String.format("%s %d", HttpStatus.SERVER_ERROR.name(), HttpStatus.SERVER_ERROR.getCode()), ioException);

			responseHeader(dos, HttpStatus.SERVER_ERROR);
		} finally {
			dos.flush();
		}
	}

	private String getFileName(InputStream inputStream) throws IOException, IllegalAccessException {
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		String firstLine = bufferedReader.readLine();

		if (firstLine == null || firstLine.isEmpty()) {
			return "";
		}

		String[] tokens = firstLine.split(" ");
		if (tokens.length < 2) {
			throw new IllegalArgumentException(String.format("RequestHandler.getFileName - 요청의 헤더 첫줄이 이상합니다. firstLine: {%s}", firstLine));
		}

		String httpRequestMethod = tokens[HTTP_METHOD_INDEX];
		if (!GET.equals(httpRequestMethod)) {
			throw new IllegalAccessException(String.format("RequestHandler.getFileName - 지원하지 않는 메소드입니다. method: {%s}", httpRequestMethod));
		}

		String fileName = tokens[PATH_INDEX];
		if (fileName == null || fileName.isEmpty() || NO_FILE_NAME.equals(fileName)) {
			return DEFAULT_FILE_NAME;
		}

		return fileName;
	}

	private byte[] getFile(String fileName) throws IOException {
		File file = new File(WEBAPP_PATH + fileName);

		if (!file.exists() || !file.canRead() || file.isDirectory()) {
			throw new IllegalArgumentException(String.format("RequestHandler.getFile - 해당 파일이 존재하지 않거나 접근할 수 없습니다. fileName: {%s}", fileName));
		}

		return Files.readAllBytes(file.toPath());
	}

	private void responseHeader(DataOutputStream dos, HttpStatus httpStatus) throws IOException {
		dos.writeBytes(String.format("HTTP/1.1 {%d} \r\n", httpStatus.getCode()));
	}

	private void response200Header(DataOutputStream dos, int lengthOfBodyContent) throws IOException {
		dos.writeBytes("HTTP/1.1 200 OK \r\n");
		dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
		dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
		dos.writeBytes("\r\n");
	}

	private void responseBody(DataOutputStream dos, byte[] body) throws IOException {
		dos.write(body, 0, body.length);
	}
}
