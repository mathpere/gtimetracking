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

import org.springframework.context.ApplicationEvent;

public class TrackEvent extends ApplicationEvent {

	public enum Event {
		CLOSE, SAVE_TRACK, TRACK_NOW, DOUBLE_CLICK, EXPORT_DATE_RANGE, LOGIN, AMEND, LOGOUT, CLEAR_DATA;
	}

	private static final long serialVersionUID = 1L;

	private final Event event;
	private final Object value;

	public TrackEvent(Object source, Event event, Object value) {
		super(source);
		this.event = event;
		this.value = value;
	}

	public Event getEvent() {
		return event;
	}

	public Object getValue() {
		return value;
	}

}