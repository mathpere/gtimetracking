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

import java.util.Date;

import org.springframework.util.StringUtils;

public class Track {

	private final static String unnull(String input) {
		if (StringUtils.hasLength(input)) {
			return input;
		} else {
			return "";
		}
	}

	private final String summary;
	private final String project;
	private final String description;
	private final Date startTime;
	private final Date endTime;

	private final boolean amendEndTimeOfLastTrack;

	public Track(String summary, String project, String description,
			Date startTime, Date endTime, boolean amendEndTimeOfLastTrack) {
		this.summary = summary;
		this.project = project;
		this.description = description;
		this.startTime = startTime;
		this.endTime = endTime;
		this.amendEndTimeOfLastTrack = amendEndTimeOfLastTrack;
	}

	public String getDescription() {
		return unnull(description);
	}

	public Date getEndTime() {
		return endTime;
	}

	public String getProject() {
		return unnull(project);
	}

	public Date getStartTime() {
		return startTime;
	}

	public String getSummary() {
		return unnull(summary);
	}

	public boolean isAmendEndTimeOfLastTrack() {
		return amendEndTimeOfLastTrack;
	}

}