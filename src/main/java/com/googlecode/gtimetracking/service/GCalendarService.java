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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.google.gdata.client.calendar.CalendarQuery;
import com.google.gdata.client.calendar.CalendarService;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.calendar.CalendarEventEntry;
import com.google.gdata.data.calendar.CalendarEventFeed;
import com.google.gdata.data.extensions.When;
import com.googlecode.gtimetracking.XlsFileWriter;
import com.googlecode.gtimetracking.vo.DateRange;
import com.googlecode.gtimetracking.vo.GCalendarCredentials;
import com.googlecode.gtimetracking.vo.Track;

public class GCalendarService {

	protected final static Logger LOG = Logger
			.getLogger(GCalendarService.class);

	private final static String EXPORT_FILENAME = "%1$tY-%1$tm-%1$td_%2$tY-%2$tm-%2$td.xls";

	private EncryptService encryptService;

	private UIService uiService;

	private String prefix;

	private final static String LINK_LABEL = "%1$tD %1$tR - %2$tD %2$tR";

	private static double round05(double d) {
		int i = (int) (d * 20);
		if (i == d * 20) {
			return d;
		}
		return (i + 1) / 20d;
	}

	public boolean canLogActivity() {
		return GCalendarCredentials.get() != null;
	}

	private CalendarService createCalendarService(
			GCalendarCredentials gcalendarCredentials) throws Exception {

		CalendarService calendarService = new CalendarService(
				"track-calendar-service");

		calendarService.setUserCredentials(gcalendarCredentials.getUsername(),
				encryptService.decryptPassword(gcalendarCredentials
						.getPassword()));

		return calendarService;
	}

	private URL createURL(GCalendarCredentials gcalendarCredentials)
			throws Exception {
		return new URL("http://www.google.com/calendar/feeds/"
				+ gcalendarCredentials.getUsername() + "/private/full");
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

					summary = summary.substring(prefix.length(), summary
							.length());

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
			fileWriter.writeHeaders(new String[] { "Activity", "Total hours",
					"Total days" });

			Set<Entry<String, Long>> entrySet = timeInSecondsByActivity
					.entrySet();

			for (Entry<String, Long> entry : entrySet) {

				String activity = entry.getKey();
				Long totalHours = entry.getValue() / 3600000;
				Double totalDays = entry.getValue() / 86400000d;

				fileWriter.writeNext(new Object[] { activity, totalHours,
						round05(totalDays) });

			}

			fileWriter.writeNext(null);

			uiService.displayTrayMessage("Activities exported!",
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
		GCalendarCredentials gCalendarCredentials = GCalendarCredentials.get();

		if (gCalendarCredentials == null) {
			throw new Exception("Google Calendar credentials are incorrect!");
		}

		return gCalendarCredentials;
	}

	public void logActivity(Track track) {

		try {
			GCalendarCredentials gcalendarCredentials = getGCalendarCredentials();
			CalendarService calendarService = createCalendarService(gcalendarCredentials);
			URL url = createURL(gcalendarCredentials);

			CalendarEventEntry trackEntry = new CalendarEventEntry();

			trackEntry.setTitle(new PlainTextConstruct(prefix
					+ track.getSummuary()));

			String content = "Project: " + track.getProject() + "\n\n"
					+ track.getDescription();

			trackEntry.setContent(new PlainTextConstruct(content));

			DateTime startTime = new DateTime(track.getStartTime(), TimeZone
					.getDefault());

			DateTime endTime = new DateTime(track.getEndTime(), TimeZone
					.getDefault());

			When eventTimes = new When();
			eventTimes.setStartTime(startTime);
			eventTimes.setEndTime(endTime);
			trackEntry.addTime(eventTimes);

			// Send the request and receive the response:
			CalendarEventEntry insertedEntry = calendarService.insert(url,
					trackEntry);

			if (insertedEntry != null) {

				uiService.displayTrayMessage("Activity tracked!",
						"Successfully added new track event", MessageType.INFO);

				uiService.addUrlMenuItem(String.format(LINK_LABEL, track
						.getStartTime(), track.getEndTime()), insertedEntry
						.getHtmlLink().getHref());
			}

		} catch (Exception e) {

			LOG.error("Error while inserting new entry", e);

			uiService.displayTrayMessage(
					"Error while inserting new track event",
					"Please consult the log for more details",
					MessageType.ERROR);
		}
	}

	@Required
	public void setEncryptService(EncryptService encryptService) {
		this.encryptService = encryptService;
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

}