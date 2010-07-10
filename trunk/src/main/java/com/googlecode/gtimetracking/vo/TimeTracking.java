package com.googlecode.gtimetracking.vo;

import org.apache.commons.lang.builder.EqualsBuilder;

public class TimeTracking {

	private final String summary;
	private final String description;
	private Long timeInSeconds = 0L;

	public TimeTracking(String summary, String description) {
		this.summary = Utils.unnull(summary);
		this.description = Utils.unnull(description, "n/a");
		this.timeInSeconds = 0L;
	}

	public void addTimeInSeconds(Long timeInSecondsToAdd) {
		this.timeInSeconds = timeInSeconds + timeInSecondsToAdd;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		} else if (this == obj) {
			return true;
		} else if (!(obj instanceof TimeTracking)) {
			return false;
		}

		TimeTracking other = (TimeTracking) obj;

		return EqualsBuilder.reflectionEquals(other.getDescription(),
				description)
				&& EqualsBuilder.reflectionEquals(other.getSummary(), summary);
	}

	public String getDescription() {
		return description;
	}

	public String getSummary() {
		return summary;
	}

	public Long getTimeInHours() {
		return timeInSeconds / 3600000;
	}

	public Long getTimeInSeconds() {
		return timeInSeconds;
	}
}
