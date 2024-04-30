package user.handler;

import static http.util.HttpUtils.*;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import common.model.Pair;
import db.DataBase;
import http.model.HttpRequest;
import http.model.HttpResponse;
import user.model.User;
import webserver.handler.Handler;

public class LogInHandler implements Handler {
	private static final Logger log = LoggerFactory.getLogger(LogInHandler.class);
	private static final String LOG_IN_FAIL_PATH = "/user/login_failed.html";
	private static final String LOG_IN_COOKIE_KEY = "logined";
	private static final String LOG_IN_COOKIE_SUCCESS_VALUE = "true";
	private static final String LOG_IN_COOKIE_FAIL_VALUE = "false";

	private static final Pair LOG_IN_SUCCESS_COOKIE = new Pair(LOG_IN_COOKIE_KEY, LOG_IN_COOKIE_SUCCESS_VALUE);
	private static final Pair LOG_IN_FAIL_COOKIE = new Pair(LOG_IN_COOKIE_KEY, LOG_IN_COOKIE_FAIL_VALUE);

	private final HttpRequest httpRequest;
	private final OutputStream outputStream;

	public LogInHandler(HttpRequest httpRequest, OutputStream outputStream) {
		this.httpRequest = httpRequest;
		this.outputStream = outputStream;
	}

	@Override
	public void handle() throws IOException {
		if (Objects.isNull(httpRequest.getFormData())) {
			throw new IllegalArgumentException(String.format("LogInHandler.handle - formData가 비었습니다. httpRequest: {%s}", httpRequest));
		}

		DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

		User user = new User(httpRequest.getFormData());

		if (DataBase.isRegisteredUser(user)) {
			HttpResponse.makeEmpty()
				.redirect(dataOutputStream, DEFAULT_PATH, LOG_IN_SUCCESS_COOKIE);
			log.debug(String.format("LogInHandler.handle - 로그인에 성공했습니다. user: {%s}", user));
		} else {
			HttpResponse.makeEmpty()
				.redirect(dataOutputStream, LOG_IN_FAIL_PATH, LOG_IN_FAIL_COOKIE);
			log.debug(String.format("LogInHandler.handle - 로그인에 실패했습니다. user: {%s}", user));
		}
	}
}
