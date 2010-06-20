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

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

@SuppressWarnings("serial")
public class TrackForm extends JPanel {

	private final JTextField summaryField = new JTextField(30);
	private final JTextField projectField = new JTextField(30);
	private final JTextArea descriptionField = new JTextArea(5, 30);

	{
		descriptionField.setWrapStyleWord(true);
		descriptionField.setLineWrap(true);
	}

	public TrackForm() {
		setLayout(new SpringLayout());

		add(new JLabel("Summary:"));
		add(summaryField);
		add(new JLabel("Project:"));
		add(projectField);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(descriptionField);
		add(new JLabel("Description:"));
		add(scrollPane);

		// Lay out the panel.
		SpringUtilities.makeCompactGrid(this, 3, 2, // rows, cols
				6, 6, // initX, initY
				6, 6); // xPad, yPad
	}

	public String getDescription() {
		return descriptionField.getText();
	}

	public String getProject() {
		return projectField.getText();
	}

	public String getSummary() {
		return summaryField.getText();
	}
}