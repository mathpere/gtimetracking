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

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

@SuppressWarnings("serial")
public class TrackForm extends JPanel implements ChangeListener {

	private final JComboBox summaryField = new JComboBox();
	private final JComboBox projectField = new JComboBox();
	private final JTextArea descriptionField = new JTextArea(5, 30);
	private final JCheckBox amendLastTrackField = new JCheckBox(
			"Amend end time of the last track");

	public TrackForm() {

		summaryField.setEditable(true);
		projectField.setEditable(true);
		// ----------------------
		descriptionField.setWrapStyleWord(true);
		descriptionField.setLineWrap(true);
		// ----------------------
		amendLastTrackField.setEnabled(false);
		amendLastTrackField.addChangeListener(this);
		// ----------------------
		setLayout(new SpringLayout());

		add(new JLabel("Summary:"));
		add(summaryField);
		add(new JLabel("Project:"));
		add(projectField);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(descriptionField);
		add(new JLabel("Description:"));
		add(scrollPane);

		add(new JLabel(""));
		add(amendLastTrackField);

		// Lay out the panel.
		SpringUtilities.makeCompactGrid(this, 4, 2, // rows, cols
				6, 6, // initX, initY
				6, 6); // xPad, yPad
	}

	public void enableAmendEndTimeOfLastTrack() {
		amendLastTrackField.setEnabled(true);
	}

	public String getDescription() {
		return descriptionField.getText();
	}

	public String getProject() {
		return (String) projectField.getSelectedItem();
	}

	public String getSummary() {
		return (String) summaryField.getSelectedItem();
	}

	public boolean isAmendEndTimeOfLastTrack() {
		return amendLastTrackField.isSelected();
	}

	public void setProjects(String[] projects) {
		projectField.removeAllItems();
		projectField.addItem("");
		for (String string : projects) {
			projectField.addItem(string);
		}
	}

	public void setSummaries(String[] summaries) {
		summaryField.removeAllItems();
		summaryField.addItem("");
		for (String string : summaries) {
			summaryField.addItem(string);
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == amendLastTrackField) {
			boolean selected = amendLastTrackField.isSelected();
			descriptionField.setEnabled(!selected);
			summaryField.setEnabled(!selected);
			projectField.setEnabled(!selected);
		}
	}
}