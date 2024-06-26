package user.handler;

import static http.util.HttpUtils.*;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import http.model.HttpRequest;
import http.model.HttpResponse;
import user.model.User;
import webserver.handler.Handler;

public class JoinHandler implements Handler {
	private static final Logger log = LoggerFactory.getLogger(JoinHandler.class);

	private final HttpRequest httpRequest;
	private final OutputStream outputStream;

	public JoinHandler(HttpRequest httpRequest, OutputStream outputStream) {
		this.httpRequest = httpRequest;
		this.outputStream = outputStream;
	}

	public void handle() throws IOException {
		if (Objects.isNull(httpRequest.getFormData())) {
			throw new IllegalArgumentException(String.format("JoinHandler.handle - formData가 비었습니다. httpRequest: {%s}", httpRequest));
		}

		DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

		User user = new User(httpRequest.getFormData());
		DataBase.addUser(user);

		HttpResponse.makeEmpty()
			.redirect(dataOutputStream, DEFAULT_PATH);

		log.info(String.format("JoinHandler.handle - user 회원가입에 성공했습니다. userMap: {%s}", DataBase.findAll()));
	}
}
