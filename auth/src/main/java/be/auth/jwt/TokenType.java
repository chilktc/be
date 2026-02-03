package be.auth.jwt;

public enum TokenType {
	ACCESS_TOKEN("access"),
	REFRESH_TOKEN("refresh");

	private final String type;

	TokenType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}
}
