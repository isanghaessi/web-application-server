package util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DecodeUtils {
	private static final Logger log = LoggerFactory.getLogger(DecodeUtils.class);

	private DecodeUtils() {
	}

	public static String decodeURI(String value) {
		try {
			return URLDecoder.decode(value, StandardCharsets.UTF_8.displayName());
		} catch (UnsupportedEncodingException unsupportedEncodingException) {
			log.error("DecodeUtils.decodeURI - 지원하지 않는 인코딩입니다.", unsupportedEncodingException);

			return value;
		}
	}
}
