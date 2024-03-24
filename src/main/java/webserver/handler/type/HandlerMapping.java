package webserver.handler.type;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import type.HttpMethod;
import type.HttpRequest;
import webserver.handler.JoinHandler;
import webserver.handler.model.HandlerKey;

public enum HandlerMapping {
	JOIN(new HandlerKey(HttpMethod.POST, "/user/create")) {
		@Override
		public void handle(HttpRequest httpRequest, OutputStream outputStream) throws IOException {
			new JoinHandler(httpRequest, outputStream).handle();
		}
	};

	private final HandlerKey handlerKey;

	private static final Map<HandlerKey, HandlerMapping> handlerMap = Arrays.stream(HandlerMapping.values())
		.collect(Collectors.toMap(HandlerMapping::getHandlerKey, Function.identity()));

	HandlerMapping(HandlerKey handlerKey) {
		this.handlerKey = handlerKey;
	}

	public abstract void handle(HttpRequest httpRequest, OutputStream outputStream) throws IOException;

	public static HandlerMapping getByHandlerKey(HandlerKey handlerKey) {
		if (!handlerMap.containsKey(handlerKey)) {
			return null;
		}

		return handlerMap.get(handlerKey);
	}

	public HandlerKey getHandlerKey() {
		return handlerKey;
	}
}
