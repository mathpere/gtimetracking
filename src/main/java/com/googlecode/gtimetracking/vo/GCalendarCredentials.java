package com.googlecode.gtimetracking.vo;

public class GCalendarCredentials {

	private final String login;
	private final String token;
	private final String tokenSecret;

	public GCalendarCredentials(String login, String token, String tokenSecret) {
		this.login = login;
		this.token = token;
		this.tokenSecret = tokenSecret;
	}

	public String getLogin() {
		return login;
	}

	public String getToken() {
		return token;
	}

	public String getTokenSecret() {
		return tokenSecret;
	}
}