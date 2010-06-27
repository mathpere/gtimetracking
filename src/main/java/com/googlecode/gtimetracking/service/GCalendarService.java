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

import java.awt.Desktop;
import java.awt.TrayIcon.MessageType;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.google.gdata.client.authn.oauth.GoogleOAuthHelper;
import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthHmacSha1Signer;
import com.google.gdata.client.calendar.CalendarQuery;
import com.google.gdata.client.calendar.CalendarService;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.calendar.CalendarEventFeed;
import com.google.gdata.data.extensions.When;
import com.google.gdata.data.extensions.Where;
import com.googlecode.gtimetracking.XlsFileWriter;
import com.googlecode.gtimetracking.vo.DateRange;
import com.googlecode.gtimetracking.vo.GCalendarCredentials;
import com.googlecode.gtimetracking.vo.Track;

public class GCalendarService {

	protected final static Logger LOG = Logger
			.getLogger(GCalendarService.class);

	private final static String EXPORT_FILENAME = "%1$tY-%1$tm-%1$td_%2$tY-%2$tm-%2$td.xls";

	private UIService uiService;

	private DataService dataService;

	private String prefix;

	private final static String LINK_LABEL = "%1$tD %1$tR - %2$tD %2$tR";

	private CalendarService createCalendarService(
			GCalendarCredentials gcalendarCredentials) throws Exception {

		GoogleOAuthParameters oauthParameters = new GoogleOAuthParameters();
		oauthParameters.setOAuthConsumerKey("anonymous");
		oauthParameters.setOAuthConsumerSecret("anonymous");
		oauthParameters.setOAuthToken(gcalendarCredentials.getToken());
		oauthParameters.setOAuthTokenSecret(gcalendarCredentials
				.getTokenSecret());

		CalendarService calendarService = new CalendarService(
				"track-calendar-service");

		calendarService.setOAuthCredentials(oauthParameters,
				new OAuthHmacSha1Signer());

		return calendarService;
	}

	private URL createURL(GCalendarCredentials gcalendarCredentials)
			throws Exception {
		return new URL("http://www.google.com/calendar/feeds/"
				+ gcalendarCredentials.getLogin() + "/private/full");
	}

	public void export(DateRange dateRange) {

		try {

			File saveIntoFile = uiService.saveIntoFile(String.format(
					EXPORT_FILENAME, dateRange.getFrom(), dateRange.getTo()));

			Assert.notNull(saveIntoFile, "File not selected");

			GCalendarCredentials gcalendarCredentials = getGCalendarCredentials();
			CalendarService calendarService = createCalendarService(gcalendarCredentials);
			URL url = createURL(gcalendarCredentials);

			CalendarQuery calendarQuery = new CalendarQuery(url);
			calendarQuery.setMinimumStartTime(new DateTime(dateRange.getFrom(),
					TimeZone.getDefault()));
			calendarQuery.setMaximumStartTime(new DateTime(dateRange.getTo(),
					TimeZone.getDefault()));

			// Send the request and receive the response:
			CalendarEventFeed resultFeed = calendarService.query(calendarQuery,
					CalendarEventFeed.class);

			List<CalendarEventEntry> entries = resultFeed.getEntries();

			Map<String, Long> timeInSecondsByActivity = new HashMap<String, Long>();

			for (CalendarEventEntry calendarEventEntry : entries) {

				String summary = calendarEventEntry.getTitle().getPlainText();

				if (summary.startsWith(prefix)) {

					summary = summary.substring(prefix.length(),
							summary.length());

					Long timeInSecond = 0L;

					try {
						When when = calendarEventEntry.getTimes().get(0);

						timeInSecond = when.getEndTime().getValue()
								- when.getStartTime().getValue();

					} catch (Exception e) {
						timeInSecond = 0L;
					}

					if (timeInSecondsByActivity.containsKey(summary)) {
						timeInSecondsByActivity.put(summary,
								timeInSecondsByActivity.get(summary)
										+ timeInSecond);
					} else {
						timeInSecondsByActivity.put(summary, timeInSecond);
					}
				}
			}

			XlsFileWriter fileWriter = new XlsFileWriter();
			fileWriter.setOutputStream(saveIntoFile);
			fileWriter.writeHeaders(new String[] { "Activity", "Total hours" });

			Set<Entry<String, Long>> entrySet = timeInSecondsByActivity
					.entrySet();

			for (Entry<String, Long> entry : entrySet) {

				String activity = entry.getKey();
				Long totalHours = entry.getValue() / 3600000;

				fileWriter.writeNext(new Object[] { activity, totalHours });

			}

			fileWriter.writeNext(null);

			uiService.displayTrayMessage(
					"Activities exported!",
					"Successfully exported activities to "
							+ saveIntoFile.getPath(), MessageType.INFO);

		} catch (Exception e) {
			LOG.error("Error while exporting data", e);

			uiService.displayTrayMessage("Error while exporting data",
					"Please consult the log for more details",
					MessageType.ERROR);
		}
	}

	private GCalendarCredentials getGCalendarCredentials() throws Exception {
		GCalendarCredentials gCalendarCredentials = dataService
				.getCredentials();

		if (gCalendarCredentials == null) {
			throw new Exception("Google Calendar credentials are incorrect!");
		}

		return gCalendarCredentials;
	}

	public void grantAccess() {

		try {

			GoogleOAuthParameters oauthParameters = new GoogleOAuthParameters();
			oauthParameters.setOAuthConsumerKey("anonymous");
			oauthParameters.setOAuthConsumerSecret("anonymous");
			oauthParameters.setScope("http://www.google.com/calendar/feeds/");

			GoogleOAuthHelper oauthHelper = new GoogleOAuthHelper(
					new OAuthHmacSha1Signer());
			oauthHelper.getUnauthorizedRequestToken(oauthParameters);
			String userAuthorizationUrl = oauthHelper
					.createUserAuthorizationUrl(oauthParameters);

			Desktop.getDesktop().browse(new URI(userAuthorizationUrl));

			String login = uiService.showGCalendarLoginForm();

			if (StringUtils.hasLength(login)) {

				oauthHelper.getAccessToken(oauthParameters);
				String accessToken = oauthParameters.getOAuthToken();
				String accessTokenSecret = oauthParameters
						.getOAuthTokenSecret();

				dataService.saveCredentials(new GCalendarCredentials(login,
						accessToken, accessTokenSecret));

				uiService.displayTrayMessage("Success!",
						"You have successfully granted access"
								+ " to your calendar", MessageType.INFO);

			} else {

				dataService.saveCredentials(new GCalendarCredentials(null,
						null, null));

				uiService.displayTrayMessage(
						"Error while granting access to your calendar",
						"You have to enter your email", MessageType.ERROR);

			}

		} catch (Exception e) {

			LOG.error("Error while granting access to your calendar", e);

			uiService.displayTrayMessage(
					"Error while granting access to your calendar",
					"Please consult the log for more details",
					MessageType.ERROR);
		}
	}

	public boolean hasAccess() {
		return dataService.getCredentials() != null;
	}

	public void setDataService(DataService dataService) {
		this.dataService = dataService;
	}

	@Required
	public void setPrefix(String prefix) {
		if (StringUtils.hasLength(prefix)) {
			if (prefix.endsWith(" ")) {
				this.prefix = prefix;
			} else {
				this.prefix = prefix + " ";
			}
		} else {
			this.prefix = prefix = "";
		}
	}

	@Required
	public void setUiService(UIService uiService) {
		this.uiService = uiService;
	}

	public void track(Track track) {

		try {
			GCalendarCredentials gcalendarCredentials = getGCalendarCredentials();
			CalendarService calendarService = createCalendarService(gcalendarCredentials);

			DateTime endTime = new DateTime(track.getEndTime(),
					TimeZone.getDefault());

			CalendarEventEntry calendarEventEntry = null;

			if (!track.isAmendEndTimeOfLastTrack()) {

				CalendarEventEntry trackEntry = new CalendarEventEntry();

				trackEntry.setTitle(new PlainTextConstruct(prefix
						+ track.getSummary()));

				trackEntry.addLocation(new Where("", "", track.getProject()));

				trackEntry.setContent(new PlainTextConstruct(track
						.getDescription()));

				DateTime startTime = new DateTime(track.getStartTime(),
						TimeZone.getDefault());

				When eventTimes = new When();
				eventTimes.setStartTime(startTime);
				eventTimes.setEndTime(endTime);
				trackEntry.addTime(eventTimes);

				// Send the request and receive the response:
				calendarEventEntry = calendarService.insert(
						createURL(gcalendarCredentials), trackEntry);

			} else {

				Track latestTrack = dataService.getLatestTrack();

				track = new Track(latestTrack.getSummary(),
						latestTrack.getProject(), latestTrack.getDescription(),
						latestTrack.getStartTime(), track.getEndTime(), false);

				CalendarEventEntry latestCalendarEventEntry = dataService
						.getLatestCalendarEventEntry();

				latestCalendarEventEntry.getTimes().get(0).setEndTime(endTime);

				calendarEventEntry = latestCalendarEventEntry.update();

			}

			if (calendarEventEntry != null) {

				dataService.saveLatestTrack(track, calendarEventEntry);

				uiService.enableAmendEndTimeOfLastTrack();

				uiService.displayTrayMessage("Activity tracked!",
						"Successfully added new track event", MessageType.INFO);

				uiService.addUrlMenuItem(String.format(LINK_LABEL,
						track.getStartTime(), track.getEndTime()),
						calendarEventEntry.getHtmlLink().getHref());
			}

		} catch (Exception e) {

			LOG.error("Error while inserting new entry", e);

			uiService.displayTrayMessage(
					"Error while inserting new track event",
					"Please consult the log for more details",
					MessageType.ERROR);
		}
	}
}