package model;

import java.util.Map;

public class User {
	private String userId;
	private String password;
	private String name;
	private String email;

	public User() {
	}

	public User(String userId, String password, String name, String email) {
		this.userId = userId;
		this.password = password;
		this.name = name;
		this.email = email;
	}

	public String getUserId() {
		return userId;
	}

	public String getPassword() {
		return password;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	@Override
	public String toString() {
		return "User [userId=" + userId + ", password=" + password + ", name=" + name + ", email=" + email + "]";
	}

	public User(Map<String, String> parameters) {
		this.userId = parameters.getOrDefault("userId", "");
		this.password = parameters.getOrDefault("password", "");
		this.name = parameters.getOrDefault("name", "");
		this.email = parameters.getOrDefault("email", "");
	}
}
