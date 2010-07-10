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
import java.util.Date;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.util.StringUtils;

import com.googlecode.gtimetracking.vo.DateRange;
import com.googlecode.gtimetracking.vo.Track;
import com.googlecode.gtimetracking.vo.TrackEvent;
import com.googlecode.gtimetracking.vo.TrackEvent.Event;

public class AppService implements ApplicationListener<ApplicationEvent> {

	private UIService uiService;
	private GoogleService googleService;
	private DataService dataService;

	private Date startTime = null;

	private boolean firstTime = true;

	public void initApp() {
		startTime = new Date();

		boolean hasAccess = googleService.hasAccess();

		uiService.enableLogin(!hasAccess);

		if (!hasAccess) {
			googleService.grantAccess();
		}
	}

	@Override
	public void onApplicationEvent(ApplicationEvent applicationEvent) {

		if (applicationEvent instanceof TrackEvent) {

			TrackEvent trackEvent = (TrackEvent) applicationEvent;
			Event event = ((TrackEvent) applicationEvent).getEvent();

			switch (event) {

			case CLOSE:

				track();
				System.exit(0);
				break;

			case SAVE_TRACK:

				Track track = (Track) trackEvent.getValue();

				if (StringUtils.hasLength(track.getSummary())) {
					googleService.track((Track) trackEvent.getValue());
					startTime = new Date();
				}

				break;

			case TRACK_NOW:

				track();
				break;

			case DOUBLE_CLICK:

				if (!googleService.hasAccess()) {
					googleService.grantAccess();
				} else {
					uiService.showTrackForm(startTime);
				}
				break;

			case EXPORT_DATE_RANGE:

				DateRange dateRange = (DateRange) trackEvent.getValue();

				if (dateRange.getFrom() != null && dateRange.getTo() != null) {
					googleService.export(dateRange);
				}

				break;

			case LOGIN:

				googleService.grantAccess();
				break;

			case LOGOUT:

				googleService.revokeAccess();
				break;

			case AMEND:

				googleService.track(new Track(null, null, null, null,
						new Date(), true));
				startTime = new Date();

				break;

			case CLEAR_DATA:

				dataService.clearData();
				uiService.displayTrayMessage("Success!",
						"Successfully cleared data", MessageType.INFO);

				break;
			}
		}
	}

	@Required
	public void setDataService(DataService dataService) {
		this.dataService = dataService;
	}

	@Required
	public void setGoogleService(GoogleService googleService) {
		this.googleService = googleService;
	}

	@Required
	public void setUiService(UIService uiService) {
		this.uiService = uiService;
	}

	public void track() {

		if (firstTime) {

			firstTime = false;

		} else if (googleService.hasAccess()) {

			uiService.showTrackForm(startTime);

		}
	}
}