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

public class DateRange {

	private final Date from;
	private final Date to;

	public DateRange(Date from, Date to) {
		this.to = to;
		this.from = from;
	}

	public Date getFrom() {
		return from;
	}

	public Date getTo() {
		return to;
	}

}
