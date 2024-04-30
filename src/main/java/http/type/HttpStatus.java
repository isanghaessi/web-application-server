package http.type;

public enum HttpStatus {
	OK(200, "OK"),
	REDIRECT(302, "Found"),
	BAD_REQUEST(400, "Bad Request"),
	NOT_FOUND(404, "Not Found"),
	NOT_ALLOWED_METHOD(405, "Method Not Allowed"),
	SERVER_ERROR(500, "Internal Server Error");

	private final int code;
	private final String message;

	HttpStatus(int code, String message) {
		this.code = code;
		this.message = message;
	}

	public int getCode() {
		return this.code;
	}

	public String getMessage() {
		return this.message;
	}
}
