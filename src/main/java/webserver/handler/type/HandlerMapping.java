package webserver.handler.type;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import model.HttpRequest;
import type.HttpMethod;
import webserver.handler.JoinHandler;
import webserver.handler.model.HandlerKey;
import webserver.handler.model.HandlerValue;

public enum HandlerMapping {
	JOIN(new HandlerKey(HttpMethod.POST, "^/user/create.*")) {
		@Override
		public void handle(HttpRequest httpRequest, OutputStream outputStream) throws IOException {
			new JoinHandler(httpRequest, outputStream).handle();
		}
	};

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
