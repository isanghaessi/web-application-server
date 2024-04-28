package webserver.type;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import http.model.HttpRequest;
import http.type.HttpMethod;
import user.handler.GetUsersHandler;
import user.handler.JoinHandler;
import user.handler.LogInHandler;
import webserver.model.HandlerKey;
import webserver.model.HandlerValue;

public enum HandlerMapping {
	JOIN(new HandlerKey(HttpMethod.POST, "^/user/create")) {
		@Override
		public void handle(HttpRequest httpRequest, OutputStream outputStream) throws IOException {
			new JoinHandler(httpRequest, outputStream).handle();
		}
	},
	LOG_IN(new HandlerKey(HttpMethod.POST, "^/user/login")) {
		@Override
		public void handle(HttpRequest httpRequest, OutputStream outputStream) throws IOException {
			new LogInHandler(httpRequest, outputStream).handle();
		}
	},
	GET_USERS(new HandlerKey(HttpMethod.GET, "^/user/list")) {
		@Override
		public void handle(HttpRequest httpRequest, OutputStream outputStream) throws IOException {
			new GetUsersHandler(httpRequest, outputStream).handle();
		}
	},
	;

	private final HandlerKey handlerKey;

	HandlerMapping(HandlerKey handlerKey) {
		this.handlerKey = handlerKey;
	}

	public abstract void handle(HttpRequest httpRequest, OutputStream outputStream) throws IOException;

	public static HandlerMapping getByHandlerKey(HandlerValue handlerValue) {
		return Arrays.stream(HandlerMapping.values())
			.filter(handlerMapping -> handlerMapping.getHandlerKey().isAcceptable(handlerValue))
			.findFirst()
			.orElse(null);
	}

	public HandlerKey getHandlerKey() {
		return handlerKey;
	}
}
