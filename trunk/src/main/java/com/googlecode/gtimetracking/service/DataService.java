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
package com.googlecode.gtimetracking.service;

import java.util.prefs.Preferences;

import org.springframework.util.StringUtils;

import com.google.gdata.data.calendar.CalendarEventEntry;
import com.googlecode.gtimetracking.vo.GCalendarCredentials;
import com.googlecode.gtimetracking.vo.Track;

public class DataService {

	private final static String LOGIN_KEY = "com.googlecode.gtimetracking.gclogin";
	private final static String TOKEN_KEY = "com.googlecode.gtimetracking.gctoken";
	private final static String TOKEN_SECRET_KEY = "com.googlecode.gtimetracking.gctokensecret";

	private final static String PROJECTS_KEY = "com.googlecode.gtimetracking.projects.v1";
	private final static String SUMMARIES_KEY = "com.googlecode.gtimetracking.summaries.v1";

	private final static DataService instance = new DataService();

	public final static DataService getDataService() {
		return instance;
	}

	private String[] summaries;
	private String[] projects;

	private CalendarEventEntry latestCalendarEventEntry;
	private Track latestTrack;

	private DataService() {
	}

	public void clearData() {
		Preferences.userRoot().put(SUMMARIES_KEY, "");
		Preferences.userRoot().put(PROJECTS_KEY, "");
	}

	public GCalendarCredentials getCredentials() {

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

	public CalendarEventEntry getLatestCalendarEventEntry() {
		return latestCalendarEventEntry;
	}

	public Track getLatestTrack() {
		return latestTrack;
	}

	public String[] getProjects() {
		return projects;
	}

	public String[] getSummaries() {
		return summaries;
	}

	public void initData() {
		summaries = StringUtils.delimitedListToStringArray(Preferences
				.userRoot().get(SUMMARIES_KEY, ""), "\t");

		projects = StringUtils.delimitedListToStringArray(Preferences
				.userRoot().get(PROJECTS_KEY, ""), "\t");
	}

	public void saveCredentials(GCalendarCredentials credentials) {

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

	public void saveLatestTrack(Track t, CalendarEventEntry e) {
		this.latestTrack = t;
		this.latestCalendarEventEntry = e;

		String project = latestTrack.getProject();

		if (StringUtils.hasLength(project)) {
			projects = StringUtils.addStringToArray(projects, project);
			projects = StringUtils.removeDuplicateStrings(projects);

			Preferences.userRoot().put(PROJECTS_KEY,
					StringUtils.arrayToDelimitedString(projects, "\t"));
		}

		String summary = latestTrack.getSummary();

		if (StringUtils.hasLength(summary)) {
			summaries = StringUtils.addStringToArray(summaries, summary);
			summaries = StringUtils.removeDuplicateStrings(summaries);

			Preferences.userRoot().put(SUMMARIES_KEY,
					StringUtils.arrayToDelimitedString(summaries, "\t"));
		}
	}
}