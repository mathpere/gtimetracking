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
package com.googlecode.gtimetracking.ui;

import java.awt.FlowLayout;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.toedter.calendar.JDateChooser;

@SuppressWarnings("serial")
public class ExportWithinDateRange extends JPanel {

	private final static Date SUBTRACT_ONE_MONTH;

	static {
		Calendar instance = Calendar.getInstance();
		instance.add(Calendar.MONTH, -1);
		SUBTRACT_ONE_MONTH = instance.getTime();
	}

	private final JDateChooser fromDateChooser = new JDateChooser(
			SUBTRACT_ONE_MONTH, "dd/MM/yyyy");

	private final JDateChooser toDateChooser = new JDateChooser(new Date(),
			"dd/MM/yyyy");

	public ExportWithinDateRange() {
		setLayout(new FlowLayout(FlowLayout.LEFT));
		add(new JLabel("From: "));
		add(fromDateChooser);
		add(new JLabel("To: "));
		add(toDateChooser);
	}

	public Date getFromDate() {
		return fromDateChooser.getDate();
	}

	public Date getToDate() {
		return toDateChooser.getDate();
	}
}