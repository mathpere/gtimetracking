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

import java.util.Date;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.util.StringUtils;

import com.googlecode.gtimetracking.vo.DateRange;
import com.googlecode.gtimetracking.vo.GCalendarCredentials;
import com.googlecode.gtimetracking.vo.Track;
import com.googlecode.gtimetracking.vo.TrackEvent;
import com.googlecode.gtimetracking.vo.TrackEvent.Event;

public class AppService implements ApplicationListener<ApplicationEvent> {

	private UIService uiService;

	private Date startTime = null;

	private GCalendarService gcalendarService;

	private boolean firstTime = true;

	public void initApp() {
		startTime = new Date();

		if (GCalendarCredentials.get() == null) {
			uiService.showGCalendarLoginForm();
		}
	}

	@Override
	public void onApplicationEvent(ApplicationEvent applicationEvent) {

		if (applicationEvent instanceof TrackEvent) {

			TrackEvent trackEvent = (TrackEvent) applicationEvent;
			Event event = ((TrackEvent) applicationEvent).getEvent();

			switch (event) {

			case ON_CLOSE:

				track();
				System.exit(0);
				break;

			case ON_SAVE_GCALENDAR_CREDENTIALS:

				GCalendarCredentials.save((GCalendarCredentials) trackEvent
						.getValue());
				break;

			case ON_SAVE_TRACK:

				Track track = (Track) trackEvent.getValue();

				if (StringUtils.hasLength(track.getSummuary())) {
					gcalendarService.logActivity((Track) trackEvent.getValue());
					startTime = new Date();
				}

				break;

			case TRACK_NOW:

				track();
				break;

			case DOUBLE_CLICK:

				if (GCalendarCredentials.get() == null) {
					uiService.showGCalendarLoginForm();
				} else {
					uiService.showTrackForm(startTime);
				}
				break;

			case ON_EXPORT_DATE_RANGE:

				DateRange dateRange = (DateRange) trackEvent.getValue();

				if (dateRange.getFrom() != null && dateRange.getTo() != null) {
					gcalendarService.export(dateRange);
				}

				break;
			}
		}
	}

	@Required
	public void setGcalendarService(GCalendarService gcalendarService) {
		this.gcalendarService = gcalendarService;
	}

	@Required
	public void setUiService(UIService uiService) {
		this.uiService = uiService;
	}

	public void track() {

		if (firstTime) {

			firstTime = false;

		} else if (gcalendarService.canLogActivity()) {

			uiService.showTrackForm(startTime);

		}
	}
}