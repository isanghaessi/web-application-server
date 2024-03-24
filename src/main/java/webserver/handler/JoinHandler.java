package webserver.handler;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;
import type.HttpRequest;
import type.HttpStatus;
import util.HttpRequestUtils;
import util.JsonUtils;

public class JoinHandler implements Handler {
	private static final Logger log = LoggerFactory.getLogger(JoinHandler.class);

	private final HttpRequest httpRequest;
	private final OutputStream outputStream;

	public JoinHandler(HttpRequest httpRequest, OutputStream outputStream) {
		this.httpRequest = httpRequest;
		this.outputStream = outputStream;
	}

	public void handle() throws IOException {
		DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

		try {
			User user = JsonUtils.parse(httpRequest.getBody(), User.class);
			DataBase.addUser(user);

			log.info("JoinHandler.handle - user 회원가입에 성공했습니다. userMap: {%s}", DataBase.findAll());
		} catch (IllegalAccessException | InstantiationException badRequestException) {
			log.error(String.format("%s %d", HttpStatus.BAD_REQUEST.name(), HttpStatus.BAD_REQUEST.getCode()), badRequestException);

			HttpRequestUtils.responseHeader(dataOutputStream, HttpStatus.BAD_REQUEST);
		}
	}
}
