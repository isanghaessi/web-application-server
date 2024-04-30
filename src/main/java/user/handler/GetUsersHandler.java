package user.handler;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import http.model.HttpRequest;
import http.model.HttpResponse;
import user.model.User;
import webserver.handler.Handler;

public class GetUsersHandler implements Handler {
	private static final Logger log = LoggerFactory.getLogger(GetUsersHandler.class);
	private static final String LOG_IN_PATH = "/user/login.html";

	private final HttpRequest httpRequest;
	private final OutputStream outputStream;

	public GetUsersHandler(HttpRequest httpRequest, OutputStream outputStream) {
		this.httpRequest = httpRequest;
		this.outputStream = outputStream;
	}

	@Override
	public void handle() throws IOException {
		DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

		if (!httpRequest.isLoggedIn()) {
			HttpResponse.makeEmpty()
				.redirect(dataOutputStream, LOG_IN_PATH);
		}

		String body = makeUserListHtml(DataBase.findAll());

		HttpResponse.makeHtmlHttpResponse(body)
			.response(dataOutputStream);

		log.info("GetUsersHandler.hanele - userList response! {}", body);
	}

	private String makeUserListHtml(Collection<User> users) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("<ul>");
		for (User user : users) {
			stringBuilder.append("<li>");
			stringBuilder.append(String.format("userId: %s - email: %s", user.getUserId(), user.getEmail()));
			stringBuilder.append("</li>");
		}
		stringBuilder.append("</ul>");

		return stringBuilder.toString();
	}
}
