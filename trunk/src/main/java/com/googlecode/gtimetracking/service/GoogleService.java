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

import java.awt.TrayIcon.MessageType;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.StringUtils;

import com.google.gdata.client.DocumentQuery;
import com.google.gdata.client.authn.oauth.GoogleOAuthHelper;
import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthHmacSha1Signer;
import com.google.gdata.client.calendar.CalendarQuery;
import com.google.gdata.client.calendar.CalendarService;
import com.google.gdata.client.docs.DocsService;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.MediaContent;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.calendar.CalendarEventFeed;
import com.google.gdata.data.docs.DocumentListEntry;
import com.google.gdata.data.docs.DocumentListEntry.MediaType;
import com.google.gdata.data.docs.DocumentListFeed;
import com.google.gdata.data.docs.FolderEntry;
import com.google.gdata.data.extensions.When;
import com.google.gdata.data.extensions.Where;
import com.google.gdata.data.media.MediaFileSource;
import com.google.gdata.util.ResourceNotFoundException;
import com.googlecode.gtimetracking.XlsFileWriter;
import com.googlecode.gtimetracking.vo.DateRange;
import com.googlecode.gtimetracking.vo.GoogleCredentials;
import com.googlecode.gtimetracking.vo.TimeTracking;
import com.googlecode.gtimetracking.vo.Track;

public class GoogleService {

	protected final static Logger LOG = Logger.getLogger(GoogleService.class);

	private final static String CALENDAR_SERVICE_URL = "https://www.google.com/calendar/feeds/%s/private/full";

	private final static String SCOPES = "https://docs.google.com/feeds/ https://www.google.com/calendar/feeds/";

	private final static String SPREADSHEET_TITLE = "tracking from %1$tY-%1$tm-%1$td to %2$tY-%2$tm-%2$td";

	private final static String GTIMETRACKING_FOLDER_TITLE = "GTimeTracking";

	private UIService uiService;

	private DataService dataService;

	private String prefix;

	private final static String LINK_LABEL = "%1$tD %1$tR - %2$tD %2$tR";

	private CalendarService createCalendarService(
			GoogleCredentials googleCredentials) throws Exception {

		GoogleOAuthParameters oauthParameters = createGoogleOAuthParameters(googleCredentials);

		CalendarService calendarService = new CalendarService(
				"gtimetracking-calendar-service");

		calendarService.setOAuthCredentials(oauthParameters,
				new OAuthHmacSha1Signer());

		return calendarService;
	}

	private URL createCalendarServiceURL(GoogleCredentials credentials)
			throws Exception {
		return new URL(String.format(CALENDAR_SERVICE_URL,
				credentials.getLogin()));
	}

	private GoogleOAuthParameters createGoogleOAuthParameters(
			GoogleCredentials credentials) {
		GoogleOAuthParameters oauthParameters = new GoogleOAuthParameters();
		oauthParameters.setOAuthConsumerKey("anonymous");
		oauthParameters.setOAuthConsumerSecret("anonymous");
		oauthParameters.setOAuthToken(credentials.getToken());
		oauthParameters.setOAuthTokenSecret(credentials.getTokenSecret());
		return oauthParameters;
	}

	private DocsService createDocsService(GoogleCredentials googleCredentials)
			throws Exception {

		GoogleOAuthParameters oauthParameters = createGoogleOAuthParameters(googleCredentials);

		DocsService docsService = new DocsService("gtimetracking-docs-service");

		docsService.setOAuthCredentials(oauthParameters,
				new OAuthHmacSha1Signer());

		return docsService;
	}

	public void export(DateRange dateRange) {

		try {

			GoogleCredentials googleCredentials = getGoogleCredentials();
			DocsService docsService = createDocsService(googleCredentials);
			CalendarService calendarService = createCalendarService(googleCredentials);

			// ------------------------------------------
			// Retrieving Calendar entries
			// ------------------------------------------
			URL url = createCalendarServiceURL(googleCredentials);

			CalendarQuery calendarQuery = new CalendarQuery(url);
			calendarQuery.setMinimumStartTime(new DateTime(dateRange.getFrom(),
					TimeZone.getDefault()));
			calendarQuery.setMaximumStartTime(new DateTime(dateRange.getTo(),
					TimeZone.getDefault()));

			// Send the request and receive the response:
			CalendarEventFeed resultFeed = calendarService.query(calendarQuery,
					CalendarEventFeed.class);

			List<CalendarEventEntry> entries = resultFeed.getEntries();

			Map<String, List<TimeTracking>> timeTrackingsBySummary = new HashMap<String, List<TimeTracking>>();

			int numberOfRows = 2; // header and grandtotal

			for (CalendarEventEntry calendarEventEntry : entries) {

				String summary = calendarEventEntry.getTitle().getPlainText();

				if (summary.startsWith(prefix)) {

					summary = summary.substring(prefix.length(),
							summary.length());

					String description = calendarEventEntry
							.getPlainTextContent();

					Long timeInSeconds = 0L;

					try {
						When when = calendarEventEntry.getTimes().get(0);

						timeInSeconds = when.getEndTime().getValue()
								- when.getStartTime().getValue();

					} catch (Exception e) {
						timeInSeconds = 0L;
					}

					TimeTracking timeTracking = new TimeTracking(summary,
							description);

					List<TimeTracking> timeTrackings = timeTrackingsBySummary
							.get(timeTracking.getSummary());

					if (timeTrackings == null) {
						timeTrackings = new ArrayList<TimeTracking>();
						timeTrackingsBySummary.put(timeTracking.getSummary(),
								timeTrackings);
						numberOfRows++; // subtotal
					}

					if (timeTrackings.contains(timeTracking)) {

						// Retrieve the original timetracking
						timeTracking = timeTrackings.get(timeTrackings
								.indexOf(timeTracking));

						timeTracking.addTimeInSeconds(timeInSeconds);

					} else {
						timeTracking.addTimeInSeconds(timeInSeconds);
						timeTrackings.add(timeTracking);
						numberOfRows++; // timetracking
					}
				}
			}

			// ------------------------------------------
			// GTimeTracking Folder
			// ------------------------------------------

			DocumentQuery query = new DocumentQuery(
					new URL(
							"https://docs.google.com/feeds/default/private/full/-/folder"));
			query.setTitleQuery(GTIMETRACKING_FOLDER_TITLE);
			query.setTitleExact(true);
			query.setMaxResults(1);
			DocumentListFeed feed = docsService.getFeed(query,
					DocumentListFeed.class);

			DocumentListEntry gtimeTrackingFolder = null;

			if (feed != null && feed.getEntries().size() == 1) {

				if (LOG.isDebugEnabled()) {
					LOG.debug("Folder already existing: has to use it");
				}

				gtimeTrackingFolder = feed.getEntries().get(0);

			} else {

				if (LOG.isDebugEnabled()) {
					LOG.debug("Folder inexisting: has to create it");
				}

				gtimeTrackingFolder = new FolderEntry();
				gtimeTrackingFolder.setTitle(new PlainTextConstruct(
						GTIMETRACKING_FOLDER_TITLE));
				gtimeTrackingFolder = docsService.insert(new URL(
						"https://docs.google.com/feeds/default/private/full/"),
						gtimeTrackingFolder);
			}

			URL gtimeTrackingFolderURL = new URL(
					((MediaContent) gtimeTrackingFolder.getContent()).getUri());

			// ------------------------------------------
			// Create Xls File
			// ------------------------------------------

			File xlsTempFile = File.createTempFile("gtimetracking", ".xls");
			XlsFileWriter fileWriter = new XlsFileWriter();
			fileWriter.setOutputStream(xlsTempFile);
			fileWriter.setColumnsSize(new int[] { 0, 15000, 0 });
			fileWriter.writeHeaders(new String[] { "Summary", "Description",
					"Total hours" });

			Long grandtotal = 0L;

			for (Entry<String, List<TimeTracking>> entry : timeTrackingsBySummary
					.entrySet()) {

				List<TimeTracking> value = entry.getValue();

				Long subtotal = 0L;

				boolean init = false;

				for (TimeTracking timeTracking : value) {

					if (!init) {

						fileWriter.writeValue(new Object[] {
								timeTracking.getSummary(),
								timeTracking.getDescription(),
								timeTracking.getTimeInHours() });
						init = true;

					} else {

						fileWriter.writeValue(new Object[] { "",
								timeTracking.getDescription(),
								timeTracking.getTimeInHours() });
					}

					subtotal += timeTracking.getTimeInHours();
				}

				grandtotal += subtotal;

				fileWriter.writeSubtotal(new Object[] { "Subtotal", "",
						subtotal });
			}

			fileWriter
					.writeTotal(new Object[] { "Grand total", "", grandtotal });

			fileWriter.writeValue(null);

			// ------------------------------------------
			// GTimeTracking SpreadSheet
			// ------------------------------------------

			String spreadSheetTitle = String.format(SPREADSHEET_TITLE,
					dateRange.getFrom(), dateRange.getTo());

			// Delete already existing spreadsheet in same folder with same
			// title:
			query = new DocumentQuery(gtimeTrackingFolderURL);
			query.setTitleQuery(spreadSheetTitle);
			query.setTitleExact(true);
			query.setMaxResults(1);
			feed = docsService.getFeed(query, DocumentListFeed.class);

			String spreadSheetLink = null;

			if (feed != null && feed.getEntries().size() == 1) {

				if (LOG.isDebugEnabled()) {
					LOG.debug("Spreadsheet <" + spreadSheetTitle
							+ "> already existing : use it");
				}

				DocumentListEntry existingEntry = feed.getEntries().get(0);
				existingEntry.setMediaSource(new MediaFileSource(xlsTempFile,
						MediaType.XLS.getMimeType()));
				existingEntry.setWritersCanInvite(false);
				spreadSheetLink = existingEntry.updateMedia(true)
						.getDocumentLink().getHref();

			} else {

				if (LOG.isDebugEnabled()) {
					LOG.debug("Inexisting spreadsheet <" + spreadSheetTitle
							+ "> : create it");
				}

				DocumentListEntry newDocument = new DocumentListEntry();
				newDocument.setFile(xlsTempFile, MediaType.XLS.getMimeType());
				newDocument.setTitle(new PlainTextConstruct(spreadSheetTitle));

				spreadSheetLink = docsService
						.insert(gtimeTrackingFolderURL, newDocument)
						.getDocumentLink().getHref();
			}

			uiService.browse(spreadSheetLink);

		} catch (Exception e) {
			LOG.error("Error while exporting data", e);

			uiService.displayTrayMessage("Error while exporting data",
					"Please consult the log for more details",
					MessageType.ERROR);
		}
	}

	private GoogleCredentials getGoogleCredentials() throws Exception {
		GoogleCredentials googleCredentials = dataService.getCredentials();

		if (googleCredentials == null) {
			throw new Exception("Google credentials are incorrect!");
		}

		return googleCredentials;
	}

	public void grantAccess() {

		try {

			GoogleOAuthParameters oauthParameters = new GoogleOAuthParameters();
			oauthParameters.setOAuthConsumerKey("anonymous");
			oauthParameters.setOAuthConsumerSecret("anonymous");
			oauthParameters.setScope(SCOPES);

			GoogleOAuthHelper oauthHelper = new GoogleOAuthHelper(
					new OAuthHmacSha1Signer());
			oauthHelper.getUnauthorizedRequestToken(oauthParameters);
			String userAuthorizationUrl = oauthHelper
					.createUserAuthorizationUrl(oauthParameters);

			uiService.browse(userAuthorizationUrl);

			String login = uiService.showGoogleLoginForm();

			if (StringUtils.hasLength(login)) {

				oauthHelper.getAccessToken(oauthParameters);
				String accessToken = oauthParameters.getOAuthToken();
				String accessTokenSecret = oauthParameters
						.getOAuthTokenSecret();

				dataService.saveCredentials(new GoogleCredentials(login,
						accessToken, accessTokenSecret));

				uiService.enableLogin(false);

				uiService.displayTrayMessage("Success!",
						"You have successfully granted access"
								+ " to your calendar and spreadsheet",
						MessageType.INFO);

			} else {

				dataService.saveCredentials(new GoogleCredentials(null, null,
						null));

				uiService.enableLogin(true);

				uiService
						.displayTrayMessage(
								"Error while granting access to your calendar and spreadsheet",
								"You have to enter your email",
								MessageType.ERROR);

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

	public void revokeAccess() {

		try {

			GoogleOAuthHelper oauthHelper = new GoogleOAuthHelper(
					new OAuthHmacSha1Signer());

			oauthHelper.revokeToken(createGoogleOAuthParameters(dataService
					.getCredentials()));

			dataService.saveCredentials(null);

			uiService.enableLogin(true);

			uiService.displayTrayMessage("Success!",
					"You have successfully revoked access"
							+ " to your calendar", MessageType.INFO);

		} catch (Exception e) {

			LOG.error("Error while revoking access to your calendar", e);

			uiService.displayTrayMessage(
					"Error while revoking access to your calendar",
					"Please consult the log for more details",
					MessageType.ERROR);
		}

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

	public void track(final Track track) {

		boolean amendEndTimeOfLastTrack = track.isAmendEndTimeOfLastTrack()
				|| track.equals(dataService.getLatestTrack());

		try {
			GoogleCredentials googleCredentials = getGoogleCredentials();
			CalendarService calendarService = createCalendarService(googleCredentials);

			DateTime endTime = new DateTime(track.getEndTime(),
					TimeZone.getDefault());

			CalendarEventEntry calendarEventEntry = null;

			Track _track = null;

			if (!amendEndTimeOfLastTrack) {

				_track = track;

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
				calendarEventEntry = calendarService
						.insert(createCalendarServiceURL(googleCredentials),
								trackEntry);

			} else {

				Track latestTrack = dataService.getLatestTrack();

				_track = new Track(latestTrack.getSummary(),
						latestTrack.getProject(), latestTrack.getDescription(),
						latestTrack.getStartTime(), track.getEndTime(), false);

				CalendarEventEntry latestCalendarEventEntry = dataService
						.getLatestCalendarEventEntry();

				latestCalendarEventEntry.getTimes().get(0).setEndTime(endTime);

				calendarEventEntry = latestCalendarEventEntry.update();

			}

			if (calendarEventEntry != null) {

				dataService.saveLatestTrack(_track, calendarEventEntry);

				uiService.enableAmendEndTimeOfLastTrack();

				uiService.displayTrayMessage("Activity tracked!",
						"Successfully added new track event", MessageType.INFO);

				uiService.addUrlMenuItem(String.format(LINK_LABEL,
						_track.getStartTime(), _track.getEndTime()),
						calendarEventEntry.getHtmlLink().getHref());
			}

		} catch (Exception e) {

			if (e instanceof ResourceNotFoundException
					&& amendEndTimeOfLastTrack
					&& ((ResourceNotFoundException) e).getResponseBody()
							.equals("No events found\n")) {

				Track latestTrack = dataService.getLatestTrack();

				dataService.saveLatestTrack(null, null);

				track(new Track(latestTrack.getSummary(),
						latestTrack.getProject(), latestTrack.getDescription(),
						latestTrack.getStartTime(), track.getEndTime(), false));

			} else {

				LOG.error("Error while inserting new entry", e);

				uiService.displayTrayMessage(
						"Error while inserting new track event",
						"Please consult the log for more details",
						MessageType.ERROR);
			}
		}
	}
}