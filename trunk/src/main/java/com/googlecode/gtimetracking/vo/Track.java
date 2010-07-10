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

import org.apache.commons.lang.builder.EqualsBuilder;

public class Track {

	private final String summary;
	private final String project;
	private final String description;
	private final Date startTime;
	private final Date endTime;

	private final boolean amendEndTimeOfLastTrack;

	public Track(String summary, String project, String description,
			Date startTime, Date endTime, boolean amendEndTimeOfLastTrack) {
		this.summary = Utils.unnull(summary);
		this.project = Utils.unnull(project);
		this.description = Utils.unnull(description);
		this.startTime = startTime;
		this.endTime = endTime;
		this.amendEndTimeOfLastTrack = amendEndTimeOfLastTrack;
	}

	public String getDescription() {
		return description;
	}

	public Date getEndTime() {
		return endTime;
	}

	public String getProject() {
		return project;
	}

	public Date getStartTime() {
		return startTime;
	}

	public String getSummary() {
		return summary;
	}

	public boolean isAmendEndTimeOfLastTrack() {
		return amendEndTimeOfLastTrack;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		} else if (this == obj) {
			return true;
		} else if (!(obj instanceof Track)) {
			return false;
		}

		Track other = (Track) obj;

		return EqualsBuilder.reflectionEquals(other.getDescription(),
				description)
				&& EqualsBuilder.reflectionEquals(other.getSummary(), summary)
				&& EqualsBuilder.reflectionEquals(other.getProject(), project);
	}
}