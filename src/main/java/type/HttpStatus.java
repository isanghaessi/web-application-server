package type;

public enum HttpStatus {
	BAD_REQUEST(400),
	NOT_FOUND(404),
	NOT_ALLOWED_METHOD(405),
	SERVER_ERROR(500);

	private final int code;

	HttpStatus(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}
}
