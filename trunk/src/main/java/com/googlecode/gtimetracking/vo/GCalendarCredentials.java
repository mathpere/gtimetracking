/**
 * Copyright 2010 Mathieu Perez
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.googlecode.gtimetracking.vo;

import java.util.prefs.Preferences;

import org.springframework.util.StringUtils;

public class GCalendarCredentials {

	private final static String USERNAME_KEY = "com.googlecode.gtimetracking.gcusername";
	private final static String PASSWORD_KEY = "com.googlecode.gtimetracking.gcpassword";

	public static GCalendarCredentials get() {

		String username = Preferences.userRoot().get(USERNAME_KEY, null);
		String password = Preferences.userRoot().get(PASSWORD_KEY, null);

		if (!StringUtils.hasLength(username)
				|| !StringUtils.hasLength(password)) {

			return null;

		} else {
			return new GCalendarCredentials(username, password);
		}

	}

	public static void save(GCalendarCredentials login) {

		if ((login == null) || !StringUtils.hasLength(login.getUsername())
				|| !StringUtils.hasLength(login.getPassword())) {
			login = new GCalendarCredentials("", "");
		}

		Preferences.userRoot().put(USERNAME_KEY, login.getUsername());
		Preferences.userRoot().put(PASSWORD_KEY, login.getPassword());
	}

	private final String username;
	private final String password;

	public GCalendarCredentials(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	public String getUsername() {
		return username;
	}

}