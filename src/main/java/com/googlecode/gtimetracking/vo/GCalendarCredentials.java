package com.googlecode.gtimetracking.vo;

import java.util.prefs.Preferences;

import org.springframework.util.StringUtils;

public class GCalendarCredentials {

	private final static String LOGIN_KEY = "com.googlecode.gtimetracking.gclogin";
	private final static String TOKEN_KEY = "com.googlecode.gtimetracking.gctoken";
	private final static String TOKEN_SECRET_KEY = "com.googlecode.gtimetracking.gctokensecret";

	public static GCalendarCredentials get() {

		String login = Preferences.userRoot().get(LOGIN_KEY, null);
		String token = Preferences.userRoot().get(TOKEN_KEY, null);
		String tokenSecret = Preferences.userRoot().get(TOKEN_SECRET_KEY, null);

		if (!StringUtils.hasLength(token)
				|| !StringUtils.hasLength(tokenSecret)
				|| !StringUtils.hasLength(login)) {

			return null;

		} else {
			return new GCalendarCredentials(login, token, tokenSecret);
		}

	}

	public static void save(GCalendarCredentials credentials) {

		if ((credentials == null)
				|| !StringUtils.hasLength(credentials.getLogin())
				|| !StringUtils.hasLength(credentials.getToken())
				|| !StringUtils.hasLength(credentials.getTokenSecret())) {
			credentials = new GCalendarCredentials("", "", "");
		}

		Preferences.userRoot().put(TOKEN_KEY, credentials.getToken());

		Preferences.userRoot().put(TOKEN_SECRET_KEY,
				credentials.getTokenSecret());

		Preferences.userRoot().put(LOGIN_KEY, credentials.getLogin());
	}

	private final String login;
	private final String token;
	private final String tokenSecret;

	public GCalendarCredentials(String login, String token, String tokenSecret) {
		this.login = login;
		this.token = token;
		this.tokenSecret = tokenSecret;
	}

	public String getToken() {
		return token;
	}

	public String getTokenSecret() {
		return tokenSecret;
	}

	public String getLogin() {
		return login;
	}
}